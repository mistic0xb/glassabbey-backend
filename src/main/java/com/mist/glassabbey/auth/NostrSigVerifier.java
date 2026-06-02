package com.mist.glassabbey.auth;

import fr.acinq.secp256k1.Secp256k1;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class NostrSigVerifier {

    // Nostr event ID is sha256 of the serialized event
    // Format: [0, pubkey, created_at, kind, tags, content]
    public boolean verify(String pubkey, Map<String, Object> event, String expectedChallenge) {
        try {
            // 1. check kind
            int kind = ((Number) event.get("kind")).intValue();
            if (kind != 27235) {
                log.warn("Invalid event kind: {}", kind);
                return false;
            }

            // 2. check created_at is recent — within 5 minutes
            long createdAt = ((Number) event.get("created_at")).longValue();
            long now = System.currentTimeMillis() / 1000;
            if (Math.abs(now - createdAt) > 300) {
                log.warn("Event too old or in future: created_at={}", createdAt);
                return false;
            }

            // 3. check challenge tag matches
            List<List<String>> tags = (List<List<String>>) event.get("tags");
            boolean challengeFound = tags.stream()
                    .anyMatch(tag -> tag.size() >= 2
                            && "challenge".equals(tag.get(0))
                            && expectedChallenge.equals(tag.get(1)));

            if (!challengeFound) {
                log.warn("Challenge tag not found or mismatch");
                return false;
            }

            // 4. verify event ID
            String eventId = (String) event.get("id");
            String computedId = computeEventId(pubkey, createdAt, kind, tags, (String) event.get("content"));
            if (!computedId.equals(eventId)) {
                log.warn("Event ID mismatch");
                return false;
            }

            // 5. verify schnorr signature
            String sig = (String) event.get("sig");
            return verifySchnorr(pubkey, eventId, sig);

        } catch (Exception e) {
            log.error("Signature verification failed", e);
            return false;
        }
    }

    private String computeEventId(
            String pubkey,
            long createdAt,
            int kind,
            List<List<String>> tags,
            String content
    ) throws Exception {
        // Nostr canonical serialization
        String serialized = String.format(
                "[0,\"%s\",%d,%d,%s,\"%s\"]",
                pubkey,
                createdAt,
                kind,
                serializeTags(tags),
                escapeJson(content)
        );

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(serialized.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(hash);
    }

    private String serializeTags(List<List<String>> tags) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < tags.size(); i++) {
            sb.append("[");
            List<String> tag = tags.get(i);
            for (int j = 0; j < tag.size(); j++) {
                sb.append("\"").append(escapeJson(tag.get(j))).append("\"");
                if (j < tag.size() - 1) sb.append(",");
            }
            sb.append("]");
            if (i < tags.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private boolean verifySchnorr(String pubkeyHex, String eventIdHex, String sigHex) {
        HexFormat hex = HexFormat.of();
        byte[] pubkeyBytes = hex.parseHex(pubkeyHex);
        byte[] msgBytes = hex.parseHex(eventIdHex);
        byte[] sigBytes = hex.parseHex(sigHex);

        // secp256k1-kmp expects 32-byte x-only pubkey (Nostr format)
        return Secp256k1.get().verifySchnorr(sigBytes, msgBytes, pubkeyBytes);
    }
}