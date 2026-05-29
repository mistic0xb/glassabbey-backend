package com.mist.glassabbey.creator.dtos;

import com.mist.glassabbey.gallery.dtos.GalleryDto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record CreatorDto(
        UUID id,

        String pubkey,

        String name,

        String picture,

        Instant createdAt,

        Instant updatedAt,

        List<GalleryDto> galleries

) {
    public CreatorDto {
        galleries = new ArrayList<>();
    }
}
