package com.mist.glassabbey.auth.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record VerifyChallengeRequest(
        @NotBlank
        String pubkey,

        @NotNull
        Map<String, Object> event,

        String name,
        String picture
) {
}
