package com.mist.glassabbey.auction;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuctionEventPublisher {
    private final SimpMessagingTemplate messaging;

    public void publishPriceUpdate(
            UUID pieceId,
            Long currentPrice,
            String bidderName,
            Long willingAmt,
            String sessionId
    ) {

        // broadcast to entire room
        messaging.convertAndSend(
                "/topic/auction/" + pieceId,
                Map.<Object, Object>of(
                        "type", "PRICE_UPDATE",
                        "currentPrice", currentPrice,
                        "bidderName", bidderName,
                        "willingAmt", willingAmt
                )
        );

        // notify the specific bidder their payment confirmed
        if (sessionId != null) {
            messaging.convertAndSendToUser(
                    sessionId,
                    "/queue/bid",
                    Map.of(
                            "type", "PAYMENT_CONFIRMED",
                            "willingAmt", willingAmt,
                            "currentPrice", currentPrice
                    )
            );
        }
    }

    public void publishBidOutbid(
            Long currentPrice,
            Long yourBid,
            String userPrincipal) {
        if (userPrincipal != null) {
            messaging.convertAndSendToUser(
                    userPrincipal,
                    "/queue/bid",
                    Map.of(
                            "type", "BID_OUTBID",
                            "currentPrice", currentPrice,
                            "yourBid", yourBid,
                            "message", "Your bid was matched. Current price is now " + currentPrice + ". Please bid again."
                    )
            );
        }
    }

    // broadcast pending bid to room
    public void publishBidPending(UUID pieceId, String bidderName, Long willingAmt) {
        messaging.convertAndSend(
                "/topic/auction/" + pieceId,
                Map.<Object, Object>of(
                        "type", "BID_PENDING",
                        "bidderName", bidderName,
                        "willingAmt", willingAmt
                )
        );
    }

    // notify specific bidder their window expired
    public void publishPaymentExpired(UUID pieceId, String bidId, String sessionId) {
        // tell the room the pending bid dropped
        messaging.convertAndSend(
                "/topic/auction/" + pieceId,
                Map.<Object, Object>of(
                        "type", "PENDING_DROPPED",
                        "bidId", bidId
                )
        );

        // tell the specific bidder
        if (sessionId != null) {
            messaging.convertAndSendToUser(
                    sessionId,
                    "/queue/bids",
                    Map.of(
                            "type", "PAYMENT_EXPIRED",
                            "bidId", bidId
                    )
            );
        }
    }

    // broadcast auction closed to room
    public void publishAuctionClosed(UUID pieceId, Long finalPrice, String winnerName) {
        messaging.convertAndSend(
                "/topic/auction/" + pieceId,
                Map.<Object, Object>of(
                        "type", "AUCTION_CLOSED",
                        "finalPrice", finalPrice,
                        "winnerName", winnerName != null ? winnerName : "No winner"
                )
        );
    }
}
