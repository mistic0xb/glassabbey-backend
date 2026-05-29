package com.mist.glassabbey.auth;

import com.mist.glassabbey.auth.dtos.AuthResponse;
import com.mist.glassabbey.auth.dtos.ChallengeDto;
import com.mist.glassabbey.auth.dtos.VerifyChallengeRequest;
import com.mist.glassabbey.creator.Creator;
import com.mist.glassabbey.creator.CreatorMapper;
import com.mist.glassabbey.creator.CreatorRepository;
import com.mist.glassabbey.creator.dtos.CreatorDto;
import com.mist.glassabbey.exception.UnauthorizedException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@Slf4j
@RestController
@RequestMapping(path = "/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CreatorMapper creatorMapper;
    private final CreatorRepository creatorRepository; //TODO: move it to service layer
    private final JwtService jwtService;

    @GetMapping(path = "/challenge")
    public ResponseEntity<ChallengeDto> getChallenge(HttpSession session) {
        ChallengeDto challenge = authService.createChallenge();

        // store in session temporarily
        session.setAttribute("pendingChallenge", challenge.challenge());
        session.setMaxInactiveInterval(300); // 5 minutes

        log.info("Challenge issued: {}", challenge);
        return ResponseEntity.ok().body(challenge);
    }

    @PostMapping(path = "/verify")
    public ResponseEntity<AuthResponse> verify(
            @Valid @RequestBody VerifyChallengeRequest request,
            HttpSession session
    ) {
        String pendingChallenge = (String) session.getAttribute("pendingChallenge");
        if (pendingChallenge == null) {
            throw new UnauthorizedException("No challenge found");
        }

        boolean valid = authService.verifyAndLogin(request, pendingChallenge);
        if (!valid) {
            throw new UnauthorizedException("Invalid signature");
        }

        session.invalidate(); // done with challenge session

        // upsert creator
        Creator creator = authService.upsertCreator(request.pubkey(), request.name(), request.picture());

        // generate jwt
        String token = jwtService.generate(creator.getId(), creator.getPubkey());
        log.info("generated jwt-token={}", token);

        log.info("Creator logged in: pubkey={}", creator.getPubkey());

        return ResponseEntity.ok().body(new AuthResponse(
                token,
                creator.getId().toString(),
                creator.getPubkey(),
                creator.getName(),
                creator.getPicture()
        ));
    }

    @GetMapping(path = "/me")
    public ResponseEntity<CreatorDto> me(
            @AuthenticationPrincipal UUID creatorId
    ) {
        Creator creator = creatorRepository.findCreatorById(creatorId)
                .orElseThrow(() -> new EntityNotFoundException("Creator not found with id: " + creatorId));
        CreatorDto creatorDto = creatorMapper.toDto(creator);
        return ResponseEntity.ok().body(creatorDto);
    }

    // TODO: remove this, frontend deletes the jwt
    @PostMapping(path = "/logout")
    public ResponseEntity<Void> logout(
            HttpServletRequest request
    ) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok().build();
    }
}