package com.mist.glassabbey.auction.dtos;

import com.mist.glassabbey.auction.AuctionStatus;

import java.time.Instant;
import java.util.UUID;

public record AuctionDto(
        UUID id,
        UUID pieceId,
        UUID galleryId,
        Long basePriceSats,
        Long currentPriceSats,
        Long submissionFeeSats,
        AuctionStatus status,
        Integer version,
        Instant closedAt,
        Instant createdAt
) {
}
