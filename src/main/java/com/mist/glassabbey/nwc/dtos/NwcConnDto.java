package com.mist.glassabbey.nwc.dtos;

import java.time.Instant;

public record NwcConnDto(
        String id,
        String walletName,
        Boolean isPrimary,
        Boolean isActive,
        Instant lastUsedAt,
        Instant createdAt
) {
}
