package com.mist.glassabbey.auth;

import com.mist.glassabbey.auth.dtos.ChallengeDto;
import com.mist.glassabbey.auth.dtos.VerifyChallengeRequest;
import com.mist.glassabbey.creator.Creator;

import java.util.Optional;

public interface AuthService {
    ChallengeDto createChallenge();

    boolean verifyAndLogin(VerifyChallengeRequest request, String expectedChallenge);

    Creator upsertCreator(String pubkey, String name, String picture);

    Optional<Creator> findById(String id);
}
