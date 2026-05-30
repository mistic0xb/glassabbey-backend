package com.mist.glassabbey.gallery.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record UpdateGalleryRequest(
        @NotBlank(message = "Title is required")
        @Size(min = 2, max = 50, message = "Gallery name must be between {min} and {max} characters")
        @Pattern(regexp = "^[A-Za-z0-9\\- ]+$", message = "Gallery name can only contain letters,numbers,spaces and hyphens")
        String title,

        @NotBlank(message = "Description is required")
        @Size(min = 10, max = 5000, message = "Description name must be between {min} and {max} characters")
        @Pattern(regexp = "^[A-Za-z0-9\\- ]+$", message = "Description can only contain letters,numbers,spaces and hyphens")
        String description,

        @NotNull(message = "Gallery auction end date is needed")
        Instant endAt
) {
    public UpdateGalleryRequest {
        if (endAt.isBefore(Instant.now())) {
            throw new IllegalArgumentException(
                    "Gallery auction end time must be in the future."
            );
        }
    }
}
