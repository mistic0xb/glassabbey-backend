package com.mist.glassabbey.bid;

import com.mist.glassabbey.auction.AuctionEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class BidScheduler {
    private final BidRepository bidRepository;
    private final AuctionEventPublisher eventPublisher;

    @Scheduled(fixedRate = 30_000) // 30s
    @Transactional
    public void expireStaleBids() {
        List<Bid> staleBids = bidRepository.findExpiredPendingBids(Instant.now());

        if (staleBids.isEmpty()) return;

        log.info("Expiring {} stale bids", Optional.of(staleBids.size()));

        staleBids.forEach(bid -> {
            bid.setStatus(BidStatus.EXPIRED);
            bidRepository.save(bid);

            // notify to the bidder
            eventPublisher.publishPaymentExpired(
                    bid.getAuction().getPiece().getId(),
                    bid.getId().toString(),
                    bid.getUserPrincipal()
            );
        });
    }
}
