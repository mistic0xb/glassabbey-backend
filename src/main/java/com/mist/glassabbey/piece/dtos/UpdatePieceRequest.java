package com.mist.glassabbey.piece.dtos;

import jakarta.validation.constraints.*;

public record UpdatePieceRequest(
        @NotBlank(message = "Title is required")
        @Size(min = 2, max = 50, message = "Piece name must be between {min} and {max} characters")
        @Pattern(regexp = "^[A-Za-z0-9\\- ]+$", message = "Piece name can only contain letters,numbers,spaces and hyphens")
        String title,

        @NotBlank(message = "Description is required")
        @Size(min = 10, max = 5000, message = "Description name must be between {min} and {max} characters")
        String description,

        @NotBlank(message = "Artist name is required")
        @Size(max = 255, message = "Artist name cannot exceed 255 characters")
        String artistName,

        String artistProfile,

        @Size(max = 100, message = "Medium description too long")
        String medium,

        @Size(max = 100, message = "Dimensions description too long")
        String dimensions,

        String imgUrl,

        @NotNull
        @Positive(message = "Base price must be positive")
        @Max(value = 100_000_000, message = "Base price too large")
        Long basePriceSats
) {
}
