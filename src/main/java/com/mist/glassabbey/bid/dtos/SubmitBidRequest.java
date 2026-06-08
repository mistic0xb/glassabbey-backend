package com.mist.glassabbey.bid.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record SubmitBidRequest(
        @NotBlank
        String bidderName,

        @NotNull @Positive
        Long bidIncrementSats,

        @NotNull
        UUID idempotencyKey
) {}
