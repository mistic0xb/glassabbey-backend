package com.mist.glassabbey.security;

import com.mist.glassabbey.creator.CreatorRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SessionAuthFilter extends OncePerRequestFilter {

    private final CreatorRepository creatorRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session != null) {
            String creatorId = (String) session.getAttribute("creatorId");

            if (creatorId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                creatorRepository.findById(UUID.fromString(creatorId))
                        .ifPresent(creator -> {
                            // wrap creator in an Authentication object
                            UsernamePasswordAuthenticationToken auth =
                                    new UsernamePasswordAuthenticationToken(
                                            creator,       // principal — accessible via @AuthenticationPrincipal
                                            null,          // credentials — not needed
                                            List.of()      // authorities — empty for now
                                    );
                            SecurityContextHolder.getContext().setAuthentication(auth);
                        });
            }
        }

        filterChain.doFilter(request, response);
    }
}
