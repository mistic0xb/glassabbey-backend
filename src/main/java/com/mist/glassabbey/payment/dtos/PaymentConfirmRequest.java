package com.mist.glassabbey.payment.dtos;

import jakarta.validation.constraints.NotBlank;

public record PaymentConfirmRequest(
        @NotBlank String paymentHash
) {
}
