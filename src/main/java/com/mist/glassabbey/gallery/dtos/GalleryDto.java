package com.mist.glassabbey.gallery.dtos;

import com.mist.glassabbey.gallery.GalleryStatus;

import java.time.Instant;

public record GalleryDto(
        String id,
        String creatorId,
        String creatorName,
        String title,
        String description,
        String coverImageUrl,
        Integer pieceCount,
        GalleryStatus status,
        Instant publishedAt,
        Instant endAt,
        Instant createdAt,
        Instant updatedAt
) {
}
