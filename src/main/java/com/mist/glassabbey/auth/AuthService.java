package com.mist.glassabbey.auth;

import com.mist.glassabbey.auth.dtos.ChallengeDto;
import com.mist.glassabbey.auth.dtos.VerifyChallengeRequest;
import com.mist.glassabbey.creator.Creator;

import java.util.Optional;
import java.util.UUID;

public interface AuthService {
    ChallengeDto createChallenge();

    boolean verifyAndLogin(VerifyChallengeRequest request, String expectedChallenge);

    // TODO: refactor this, move to creator service
    Creator upsertCreator(String pubkey, String name, String picture);

    Optional<Creator> findById(UUID id);
}
