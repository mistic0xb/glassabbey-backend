package com.mist.glassabbey.nwc;

import com.mist.glassabbey.creator.Creator;
import com.mist.glassabbey.creator.CreatorRepository;
import com.mist.glassabbey.exception.NwcException;
import com.mist.glassabbey.nwc.dtos.NwcConnDto;
import com.mist.glassabbey.nwc.dtos.RegisterNwcRequest;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/nwc")
@RequiredArgsConstructor
public class NwcController {

    private final NwcService nwcService;
    private final NwcConnRepository nwcConnRepository;
    private final NwcConnMapper nwcConnMapper;
    private final NwcEncryptionService encryptionService;
    private final CreatorRepository creatorRepository;

    // POST /api/v1/nwc/register
    // creator registers a new NWC connection
    @PostMapping("/register")
    @Transactional
    public ResponseEntity<NwcConnDto> register(
            @AuthenticationPrincipal UUID creatorId,
            @Valid @RequestBody RegisterNwcRequest request
    ) {
        // TODO: move this register logic to nwcService layer

        Creator creator = creatorRepository.findById(creatorId)
                .orElseThrow(() -> new EntityNotFoundException("Creator not found"));

        // validate NWC string before storing
        try {
            NwcConfig.parse(request.nwcString());
        } catch (NwcException e) {
            return ResponseEntity.badRequest().build();
        }

        // if this is set as primary, demote existing primary
        if (request.isPrimary()) {
            nwcConnRepository.demotePrimary(creatorId);
        }

        // if creator has no connections yet, make this primary automatically
        boolean hasPrimary = nwcConnRepository
                .findPrimaryByGalleryCreatorId(creatorId)
                .isPresent();

        NwcConn conn = NwcConn.builder()
                .creator(creator)
                .walletName(request.walletName())
                .encryptedNwc(request.nwcString()) //TODO: encrypt this nwc string
                .isPrimary(request.isPrimary() || !hasPrimary)
                .isActive(true)
                .build();

        NwcConn saved = nwcConnRepository.save(conn);
        log.info("NWC registered for creator={}, wallet={}, primary={}",
                creatorId, request.walletName(), saved.getIsPrimary());

        return ResponseEntity.status(HttpStatus.CREATED).body(nwcConnMapper.toDto(saved));
    }

    // GET /api/v1/nwc
    // list creator's NWC connections
    @GetMapping
    public ResponseEntity<List<NwcConnDto>> list(
            @AuthenticationPrincipal UUID creatorId
    ) {
        List<NwcConnDto> conns = nwcConnRepository
                .findByCreatorIdOrderByCreatedAtDesc(creatorId)
                .stream()
                .map(nwcConnMapper::toDto)
                .toList();
        return ResponseEntity.ok(conns);
    }

    // PATCH /api/v1/nwc/{connId}/primary
    // set a connection as primary
    @PatchMapping("/{connId}/primary")
    public ResponseEntity<NwcConnDto> setPrimary(
            @AuthenticationPrincipal String creatorId,
            @PathVariable UUID connId
    ) {
        NwcConn conn = nwcConnRepository
                .findByIdAndCreatorId(connId, UUID.fromString(creatorId))
                .orElseThrow(() -> new EntityNotFoundException("NWC connection not found"));

        nwcConnRepository.demotePrimary(UUID.fromString(creatorId));
        conn.setIsPrimary(true);
        nwcConnRepository.save(conn);

        return ResponseEntity.ok(nwcConnMapper.toDto(conn));
    }

    // PATCH /api/v1/nwc/{connId}/deactivate
    // deactivate a connection
    @PatchMapping("/{connId}/deactivate")
    public ResponseEntity<NwcConnDto> deactivate(
            @AuthenticationPrincipal String creatorId,
            @PathVariable UUID connId
    ) {
        NwcConn conn = nwcConnRepository
                .findByIdAndCreatorId(connId, UUID.fromString(creatorId))
                .orElseThrow(() -> new EntityNotFoundException("NWC connection not found"));

        conn.setIsActive(false);
        nwcConnRepository.save(conn);

        return ResponseEntity.ok(nwcConnMapper.toDto(conn));
    }

    // DELETE /api/v1/nwc/{connId}
    @DeleteMapping("/{connId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal String creatorId,
            @PathVariable UUID connId
    ) {
        NwcConn conn = nwcConnRepository
                .findByIdAndCreatorId(connId, UUID.fromString(creatorId))
                .orElseThrow(() -> new EntityNotFoundException("NWC connection not found"));

        nwcConnRepository.delete(conn);
        return ResponseEntity.noContent().build();
    }
}
