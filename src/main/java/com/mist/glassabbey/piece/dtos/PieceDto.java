package com.mist.glassabbey.piece.dtos;

import java.time.Instant;
import java.util.UUID;

public record PieceDto(
        UUID id,
        UUID galleryId,
        String title,
        String description,
        String imgUrl,
        String basePriceSats,
        Instant createdAt,
        Instant updatedAt
) {
}
