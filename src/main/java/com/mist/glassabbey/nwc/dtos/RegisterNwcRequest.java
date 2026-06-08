package com.mist.glassabbey.nwc.dtos;

import jakarta.validation.constraints.NotBlank;

public record RegisterNwcRequest(
        @NotBlank
        String walletName,

        @NotBlank
        String nwcString,

        boolean isPrimary
) {
}
