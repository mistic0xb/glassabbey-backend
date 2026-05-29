package com.mist.glassabbey.gallery.dtos;

import java.time.Instant;

public record GalleryDto(
        String id,
        String creatorId,
        String creatorName,
        String title,
        String description,
        Integer pieceCount,
        String status,
        Instant publishedAt,
        Instant endAt,
        Instant createdAt,
        Instant updatedAt
) {
}
