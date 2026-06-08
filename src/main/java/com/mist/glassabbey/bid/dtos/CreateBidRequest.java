package com.mist.glassabbey.bid.dtos;

import com.mist.glassabbey.bid.BidStatus;
import com.mist.glassabbey.nwc.NwcConn;

import java.time.Instant;
import java.util.UUID;

public record CreateBidRequest(
        UUID auctionId,
        NwcConn nwcConnId,
        String sessionId,
        UUID idempotencyKey,
        String bidderName,
        Long bidIncrementSats,
        Long willingAmtSats,
        String paymentHash,
        String paymentRequest,
        BidStatus status,
        Instant createdAt,
        Instant confirmedAt,
        Instant expiresAt
) {
}
