package com.mist.glassabbey.auth.dtos;

import java.util.List;

public record NostrEventDto(
        String id,
        String pubkey,
        long created_at,
        int kind,
        List<List<String>> tags,
        String content,
        String sig
) {
}
