package com.mist.glassabbey.bid.dtos;

import java.time.Instant;

public record BidAcceptedResponse(
        String bidId,
        String bidderName,
        Long willingAmtSats,
        Long submitAmtSats,
        String paymentRequest,
        Instant expiresAt
) {}
