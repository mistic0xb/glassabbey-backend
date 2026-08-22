package com.mist.glassabbey.auction.dtos;

import com.mist.glassabbey.auction.AuctionStatus;
import com.mist.glassabbey.gallery.dtos.GalleryDto;
import com.mist.glassabbey.piece.dtos.PieceDto;

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
        PieceDto piece,
        GalleryDto gallery,
        Integer version,
        Instant closedAt,
        Instant createdAt
) {
}
