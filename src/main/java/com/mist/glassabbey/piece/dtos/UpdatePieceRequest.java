package com.mist.glassabbey.piece.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdatePieceRequest(
        @NotBlank(message = "Title is required")
        @Size(min = 2, max = 50, message = "Piece name must be between {min} and {max} characters")
        @Pattern(regexp = "^[A-Za-z0-9\\- ]+$", message = "Piece name can only contain letters,numbers,spaces and hyphens")
        String title,

        @NotBlank(message = "Description is required")
        @Size(min = 10, max = 5000, message = "Description name must be between {min} and {max} characters")
        String description,

        String imgUrl

) {
}
