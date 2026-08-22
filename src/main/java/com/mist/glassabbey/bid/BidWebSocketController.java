package com.mist.glassabbey.bid;

import com.mist.glassabbey.auction.Auction;
import com.mist.glassabbey.auction.AuctionRepository;
import com.mist.glassabbey.bid.dtos.BidAcceptedResponse;
import com.mist.glassabbey.bid.dtos.BidDto;
import com.mist.glassabbey.bid.dtos.SubmitBidRequest;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class BidWebSocketController {
    private final BidService bidService;
    private final AuctionRepository auctionRepository;
    private final SimpMessagingTemplate messaging;

    // on subscribe -> push current state
    @EventListener
    public void onSubscribe(SessionSubscribeEvent event) {
        StompHeaderAccessor headers = StompHeaderAccessor.wrap(event.getMessage());
        String destination = headers.getDestination();

        // only handle auction room subs
        if (destination == null || !destination.startsWith("/topic/auction")) return;
        String pieceId = destination.replace("/topic/auction/", "").trim();

        String principal = headers.getUser() != null ? headers.getUser().getName() : null;
        if (principal == null) return;

        log.info("[STOMP Subscribe] destination='{}' | pieceId='{}' | principal='{}'", destination, pieceId, principal);

        try {
            Auction auction = auctionRepository.findByPieceId(UUID.fromString(pieceId))
                    .orElseThrow(() -> new EntityNotFoundException("No auction has piece with pieceId: +" + pieceId));

            List<BidDto> topBidders = bidService.getLeaderBoard(auction.getId());

            // push state to this client only
            messaging.convertAndSendToUser(
                    principal,
                    "/queue/bid",
                    Map.of(
                            "type", "STATE",
                            "auctionId", auction.getId().toString(),
                            "pieceId", pieceId,
                            "currentPrice", auction.getCurrentPriceSats(),
                            "status", auction.getStatus().name(),
                            "topBidders", topBidders
                    )
            );
            log.info("STATE PUSHED: pieceId={}, principal={}", pieceId, principal);
        } catch (Exception e) {
            log.error("[{}] Failed to push state on subscribe: {}", pieceId, e.getMessage());
        }
    }

    // submit bid
    @MessageMapping("/auction/{pieceId}/bid")
    public void bid(
            @DestinationVariable UUID pieceId,
            @Payload SubmitBidRequest request,
            SimpMessageHeaderAccessor headers,
            Principal principal
    ) {
        String sessionId = headers.getSessionId();
        String userPrincipal = (principal != null) ? principal.getName() : sessionId;
        log.info("SUBMIT_BID pieceId={}, sessionId={},userPrincipal={}, bidder={}", pieceId, sessionId, userPrincipal, request.bidderName());

        try {
            BidAcceptedResponse result = bidService.submitBid(pieceId, request, userPrincipal);
            log.info("BID_ACCEPTED for user={}", userPrincipal);

            assert userPrincipal != null;
            messaging.convertAndSendToUser(
                    userPrincipal,
                    "/queue/bid",
                    Map.of(
                            "type", "BID_ACCEPTED",
                            "bidId", result.bidId(),
                            "bidderName", result.bidderName(),
                            "willingAmtSats", result.willingAmtSats(),
                            "submitAmtSats", result.submitAmtSats(),
                            "paymentRequest", result.paymentRequest(),
                            "expiresAt", result.expiresAt().toString()
                    )
            );

        } catch (Exception e) {
            log.warn("BID_REJECTED: pieceId={}, reason={}", pieceId, e.getMessage());
            assert userPrincipal != null;
            messaging.convertAndSendToUser(
                    userPrincipal,
                    "/queue/bid",
                    Map.of(
                            "type", "BID_REJECTED",
                            "reason", e.getMessage()
                    )
            );
        }
    }

    // cancel bid
    @MessageMapping("/auction/{pieceId}/cancel")
    public void cancel(
            @DestinationVariable UUID pieceId,
            @Payload Map<String, String> payload,
            SimpMessageHeaderAccessor headers,
            Principal principal
    ) {
        String bidId = payload.get("bidId");

        if (bidId == null) {
            log.warn("CANCEL_BID missing bidId, pieceId={}", pieceId);
            return;
        }

        String userPrincipal = principal.getName();
        log.info("[CANCEL_BID] pieceId={} | bidId={} | principal={}", pieceId, bidId, userPrincipal);

        try {
            bidService.cancelBid(UUID.fromString(bidId), userPrincipal);
        } catch (Exception e) {
            log.warn("Cancel failed: {}", e.getMessage());
        }
    }
}
