package com.mist.glassabbey.piece.dtos;

import java.time.Instant;
import java.util.UUID;

public record PieceDto(
        UUID id,
        UUID galleryId,
        String title,
        String description,
        String artistName,
        String artistProfile,
        String medium,
        String dimensions,
        String imgUrl,
        Long basePriceSats,
        Instant createdAt,
        Instant updatedAt
) {
}
