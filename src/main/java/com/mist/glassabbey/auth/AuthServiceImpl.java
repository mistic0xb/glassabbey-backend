package com.mist.glassabbey.auth;

import com.mist.glassabbey.auth.dtos.ChallengeDto;
import com.mist.glassabbey.auth.dtos.VerifyChallengeRequest;
import com.mist.glassabbey.creator.Creator;
import com.mist.glassabbey.creator.CreatorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final CreatorRepository creatorRepository;
    private final NostrSigVerifier nostrSigVerifier;

    @Override
    public ChallengeDto createChallenge() {
        return new ChallengeDto(UUID.randomUUID().toString());
    }

    @Override
    public boolean verifyAndLogin(VerifyChallengeRequest request, String expectedChallenge) {
        return nostrSigVerifier.verify(request.pubkey(), request.event(), expectedChallenge);
    }

    @Transactional
    @Override
    public Creator upsertCreator(String pubkey, String name, String picture) {
        return creatorRepository.findByPubkey(pubkey)
                .map(existing -> {
                    existing.setName(name);
                    existing.setPicture(picture);
                    existing.setUpdatedAt(Instant.now());
                    return creatorRepository.save(existing);
                })
                .orElseGet(() -> {
                    Creator creator = new Creator().builder()
                            .pubkey(pubkey)
                            .name(name)
                            .picture(picture)
                            .createdAt(Instant.now())
                            .updatedAt(Instant.now())
                            .build();
                    log.info("New creator registered: pubkey={}", pubkey);
                    return creatorRepository.save(creator);
                });
    }

    @Override
    public Optional<Creator> findById(String id) {
       return creatorRepository.findById(UUID.fromString(id));
    }
}
