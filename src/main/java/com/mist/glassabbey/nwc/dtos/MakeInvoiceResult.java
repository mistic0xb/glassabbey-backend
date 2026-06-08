package com.mist.glassabbey.nwc.dtos;

public record MakeInvoiceResult(
        String invoice,
        String paymentHash
) {}
