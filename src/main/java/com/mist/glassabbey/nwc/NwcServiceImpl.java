package com.mist.glassabbey.nwc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mist.glassabbey.exception.NwcException;
import com.mist.glassabbey.nwc.dtos.InvoiceStatus;
import com.mist.glassabbey.nwc.dtos.MakeInvoiceResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nostr.base.PrivateKey;
import nostr.base.PublicKey;
import nostr.client.springwebsocket.NostrRelayClient;
import nostr.encryption.MessageCipher;
import nostr.encryption.MessageCipher04;
import nostr.event.filter.EventFilter;
import nostr.event.filter.Filters;
import nostr.event.impl.GenericEvent;
import nostr.event.message.EventMessage;
import nostr.event.message.ReqMessage;
import nostr.event.tag.GenericTag;
import nostr.id.Identity;
import nostr.util.NostrUtil;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class NwcServiceImpl implements NwcService {

    private final NwcEncryptionService encryptionService;
    private final ObjectMapper objectMapper;

    @Override
    public MakeInvoiceResult makeInvoice(NwcConn nwcConn, long amountSats, String memo) {
        Map<String, Object> params = new HashMap<>();
        params.put("amount", amountSats * 1000L);
        params.put("description", memo != null ? memo : "Bid payment");
        params.put("expiry", 3600);

        Map<String, Object> result = sendNwcRequest(nwcConn, "make_invoice", params);

        String invoice = (String) result.get("invoice");
        String paymentHash = (String) result.get("payment_hash");

        if (invoice == null || paymentHash == null) {
            throw new NwcException("make_invoice response missing invoice or payment_hash");
        }

        log.info("Invoice generated: hash={}, sats={}", paymentHash, amountSats);
        return new MakeInvoiceResult(invoice, paymentHash);
    }

    @Override
    public InvoiceStatus lookupInvoice(NwcConn nwcConn, String paymentHash) {
        try {
            Map<String, Object> result = sendNwcRequest(
                    nwcConn,
                    "lookup_invoice",
                    Map.of("payment_hash", paymentHash)
            );
            String state = (String) result.getOrDefault("state", "pending");
            return InvoiceStatus.fromString(state);
        } catch (NwcException e) {
            log.warn("lookup_invoice failed for hash={}: {}", paymentHash, e.getMessage());
            return InvoiceStatus.UNKNOWN;
        }
    }

    private Map<String, Object> sendNwcRequest(
            NwcConn nwcConn, String method, Map<String, Object> params) {

        String rawNwc = encryptionService.decrypt(nwcConn.getEncryptedNwc());
        NwcConfig config = NwcConfig.parse(rawNwc);

        // Create client identity from secret
        Identity clientIdentity = Identity.create(new PrivateKey(config.getSecret()));
        String clientPubkeyHex = clientIdentity.getPublicKey().toString();

        // Create wallet public key
        PublicKey walletPubKey = new PublicKey(config.getWalletPubkey());

        log.info("Client pubkey: {}", clientPubkeyHex.substring(0, 8) + "...");
        log.info("Wallet pubkey: {}", config.getWalletPubkey().substring(0, 8) + "...");

        // Prepare request payload
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("method", method);
        requestBody.put("params", params);

        String payload;
        try {
            payload = objectMapper.writeValueAsString(requestBody);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        log.info("Request payload: {}", payload);

        MessageCipher cipher;
        try {
            byte[] senderPrivateKey = NostrUtil.hexToBytes(config.getSecret());
            byte[] recipientPublicKey = NostrUtil.hexToBytes(config.getWalletPubkey());

            cipher = new MessageCipher04(senderPrivateKey, recipientPublicKey);
            log.info("Created NIP-04 cipher instance");
        } catch (Exception e) {
            throw new NwcException("Failed to create NIP-44 cipher: " + e.getMessage(), e);
        }

        // Encrypt the payload
        String encrypted;
        try {
            encrypted = cipher.encrypt(payload);
            log.info("Encrypted payload, length: {}", encrypted.length());
        } catch (Exception e) {
            throw new NwcException("Failed to encrypt request: " + e.getMessage(), e);
        }

        // Create and sign request event
        GenericEvent requestEvent = GenericEvent.builder()
                .pubKey(clientIdentity.getPublicKey())
                .kind(23194)
                .content(encrypted)
                .tags(List.of(
                        GenericTag.of("p", config.getWalletPubkey())
                ))
                .build();

        try {
            clientIdentity.sign(requestEvent);
            log.info("Request event signed, id: {}", requestEvent.getId().substring(0, 8) + "...");
        } catch (Exception e) {
            throw new NwcException("Failed to sign event", e);
        }

        // Send request and wait for response
        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();

        try (NostrRelayClient client = new NostrRelayClient(config.getRelayUrl())) {

            // Subscribe for response
            EventFilter filter = EventFilter.builder()
                    .kinds(List.of(23195))
                    .authors(List.of(config.getWalletPubkey()))
                    .addTagFilter("p", List.of(clientPubkeyHex))
                    .addTagFilter("e", List.of(requestEvent.getId()))
                    .since(System.currentTimeMillis() / 1000 - 10)
                    .build();

            String subscriptionId = "nwc-" + requestEvent.getId().substring(0, 8);
            log.info("Subscribing with id: {}", subscriptionId);

            client.subscribe(
                    new ReqMessage(subscriptionId, new Filters(filter).getFilters()).encode(),

                    rawMsg -> {
                        if (future.isDone()) return;

                        try {
                            log.debug("Received raw message: {}", rawMsg);

                            List<Object> data = objectMapper.readValue(rawMsg,
                                    objectMapper.getTypeFactory().constructCollectionType(List.class, Object.class));

                            if (data.size() < 3 || !"EVENT".equals(data.get(0))) {
                                return;
                            }

                            @SuppressWarnings("unchecked")
                            Map<String, Object> eventMap = (Map<String, Object>) data.get(2);

                            String content = (String) eventMap.get("content");
                            if (content == null) {
                                log.warn("No content in response event");
                                return;
                            }

                            log.info("Received response event, content length: {}", content.length());

                            // Decrypt response using the same cipher
                            String decrypted;
                            try {
                                decrypted = cipher.decrypt(content);
                                log.info("Decrypted response: {}", decrypted);
                            } catch (Exception e) {
                                log.error("Failed to decrypt response", e);
                                future.completeExceptionally(new NwcException("Decryption failed: " + e.getMessage()));
                                return;
                            }

                            @SuppressWarnings("unchecked")
                            Map<String, Object> parsed = objectMapper.readValue(decrypted,
                                    objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class));

                            // Check for error
                            if (parsed.containsKey("error") && parsed.get("error") != null) {
                                @SuppressWarnings("unchecked")
                                Map<String, Object> error = (Map<String, Object>) parsed.get("error");
                                String code = (String) error.get("code");
                                String message = (String) error.get("message");
                                log.error("NWC error - Code: {}, Message: {}", code, message);
                                future.completeExceptionally(new NwcException("NWC error: " + code + " - " + message));
                                return;
                            }

                            // Success
                            @SuppressWarnings("unchecked")
                            Map<String, Object> result = (Map<String, Object>) parsed.get("result");
                            log.info("Request successful, result type: {}", parsed.get("result_type"));
                            future.complete(result);

                        } catch (Exception e) {
                            log.error("Failed to process response", e);
                            future.completeExceptionally(new NwcException("Failed to process response: " + e.getMessage()));
                        }
                    },

                    ex -> {
                        log.error("Relay error", ex);
                        if (!future.isDone()) {
                            future.completeExceptionally(new NwcException("Relay error: " + ex.getMessage()));
                        }
                    },

                    () -> {
                        if (!future.isDone()) {
                            log.warn("Relay closed before receiving response");
                            future.completeExceptionally(new NwcException("Relay closed before response"));
                        }
                    }
            );

            // Send request
            client.send(new EventMessage(requestEvent));
            log.info("Request sent to relay");

            // Wait for response with timeout
            return future.get(25, TimeUnit.SECONDS);

        } catch (java.util.concurrent.TimeoutException e) {
            log.error("NWC request timed out after 25 seconds");
            throw new NwcException("NWC request timed out");
        } catch (Exception e) {
            Throwable cause = e instanceof java.util.concurrent.ExecutionException ? e.getCause() : e;
            if (cause instanceof NwcException ne) throw ne;
            log.error("NWC request failed", cause);
            throw new NwcException("NWC failed: " + cause.getMessage(), cause);
        }
    }
}
