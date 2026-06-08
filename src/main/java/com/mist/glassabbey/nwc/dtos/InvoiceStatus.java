package com.mist.glassabbey.nwc.dtos;

public enum InvoiceStatus {
    PENDING,
    SETTLED,
    EXPIRED,
    FAILED,
    UNKNOWN;   // wallet unreachable — polling will retry

    public static InvoiceStatus fromString(String state) {
        return switch (state.toLowerCase()) {
            case "settled" -> SETTLED;
            case "expired" -> EXPIRED;
            case "failed" -> FAILED;
            case "pending", "accepted" -> PENDING;
            default -> UNKNOWN;
        };
    }
}
