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
        String sessionId = headers.getSessionId();

        // only handle auction room subs
        if (destination == null || !destination.startsWith("/topic/auction")) return;

        String pieceId = destination.replace("/topic/auction", "");

        try {
            Auction auction = auctionRepository.findByPieceId(UUID.fromString(pieceId))
                    .orElseThrow(() -> new EntityNotFoundException("No auction has piece with pieceId: +" + pieceId));

            List<BidDto> topBidders = bidService.getLeaderBoard(auction.getId());

            // push state to this client only
            assert sessionId != null;
            messaging.convertAndSendToUser(
                    sessionId,
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
            log.info("[{}] State pushed to session: {}", pieceId, sessionId);
        } catch (Exception e) {
            log.error("[{}] Failed to push state on subscribe: {}", pieceId, e.getMessage());
        }
    }

    // submit bid
    @MessageMapping("/auction/{pieceId}/bid")
    public void submitBid(
            @DestinationVariable UUID pieceId,
            @Payload SubmitBidRequest request,
            SimpMessageHeaderAccessor headers
    ) {
        String sessionId = headers.getSessionId();
        log.info("[{}] SUBMIT_BID from session={}, bidder={}", pieceId, sessionId, request.bidderName());

        try {
            BidAcceptedResponse result = bidService.submitBid(
                    pieceId,
                    request,
                    sessionId
            );

            // tell this bidder their bid was accepted + invoice
            assert sessionId != null;
            messaging.convertAndSendToUser(
                    sessionId,
                    "/queue/bid",
                    Map.of(
                            "type", BidStatus.ACCEPTED,
                            "bidId", result.bidId(),
                            "bidderName", result.bidderName(),
                            "willingAmtSats", result.willingAmtSats(),
                            "submitAmtSats", result.submitAmtSats(),
                            "paymentRequest", result.paymentRequest(),
                            "expiresAt", result.expiresAt().toString()
                    )
            );

        } catch (Exception e) {
            log.warn("[{}] BID_REJECTED — session={}, reason={}", pieceId, sessionId, e.getMessage());
            assert sessionId != null;
            messaging.convertAndSendToUser(
                    sessionId,
                    "/queue/bid",
                    Map.of(
                            "type", BidStatus.REJECTED,
                            "reason", e.getMessage()
                    )
            );
        }
    }
}
