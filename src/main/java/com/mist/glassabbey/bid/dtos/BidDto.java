package com.mist.glassabbey.bid.dtos;

import com.mist.glassabbey.bid.BidStatus;
import com.mist.glassabbey.nwc.NwcConn;

import java.time.Instant;
import java.util.UUID;

public record BidDto(
        UUID id,
        UUID auctionId,
        String bidderName,
        Long willingAmtSats,
        BidStatus status,
        Instant createdAt,
        Instant confirmedAt,
        Instant expiresAt

) {
}
