package com.mist.glassabbey.auth;

import com.mist.glassabbey.auth.dtos.ChallengeDto;
import com.mist.glassabbey.auth.dtos.VerifyChallengeRequest;
import com.mist.glassabbey.creator.Creator;
import com.mist.glassabbey.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping(path = "/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

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
    public ResponseEntity<?> verify(
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

        // remove used challenge
        session.removeAttribute("pendingChallenge");

        // upsert creator
        Creator creator = authService.upsertCreator(request.pubkey(), request.name(), request.picture());

        // replacing temp challenge session with proper auth session
        session.setAttribute("creatorId", creator.getId().toString());
        session.setAttribute("pubkey", creator.getPubkey());

        log.info("Creator logged in: pubkey={}", creator.getPubkey());

        return ResponseEntity.ok().body(creator);
    }

    @GetMapping(path = "/me")
    public ResponseEntity<Creator> me(
            @AuthenticationPrincipal Creator creator
    ) {
        return ResponseEntity.ok().body(creator);
    }

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