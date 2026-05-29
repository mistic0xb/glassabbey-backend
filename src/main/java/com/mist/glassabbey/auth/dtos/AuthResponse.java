package com.mist.glassabbey.auth.dtos;

public record AuthResponse(
        String token,
        String id,
        String pubkey,
        String name,
        String picture
) {
}