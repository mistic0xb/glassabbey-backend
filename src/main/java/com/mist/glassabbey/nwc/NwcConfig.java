package com.mist.glassabbey.nwc;

import com.mist.glassabbey.exception.NwcException;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NwcConfig {
    private final String walletPubkey;
    private final String relayUrl;
    private final String secret;        // hex privkey

    public static NwcConfig parse(String nwcString) {
        String str = nwcString.trim();
        if (!str.startsWith("nostr+walletconnect://")) {
            throw new NwcException("Invalid NWC string");
        }

        String withoutScheme = str.substring("nostr+walletconnect://".length());
        int qMark = withoutScheme.indexOf('?');
        if (qMark < 0) throw new NwcException("Invalid NWC string: missing query params");

        String walletPubkey = withoutScheme.substring(0, qMark);
        String query = withoutScheme.substring(qMark + 1);

        String relayUrl = null;
        String secret = null;

        for (String param : query.split("&")) {
            String[] kv = param.split("=", 2);
            if (kv.length < 2) continue;
            if ("relay".equals(kv[0]))
                relayUrl = java.net.URLDecoder.decode(kv[1], java.nio.charset.StandardCharsets.UTF_8);
            if ("secret".equals(kv[0])) secret = kv[1];
        }

        if (relayUrl == null) throw new NwcException("Missing relay in NWC string");
        if (secret == null) throw new NwcException("Missing secret in NWC string");

        return NwcConfig.builder()
                .walletPubkey(walletPubkey)
                .relayUrl(relayUrl)
                .secret(secret)
                .build();
    }
}
