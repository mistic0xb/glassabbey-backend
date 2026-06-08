package com.mist.glassabbey.bid;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BidRepository extends JpaRepository<Bid, UUID> {

    // idempotency check — same request sent twice
    Optional<Bid> findByIdempotencyKey(UUID idempotencyKey);

    // payment confirmation
    Optional<Bid> findByPaymentHash(String paymentHash);

    // check if bidder already has a pending bid in this auction
    boolean existsByAuctionIdAndBidderNameAndStatus(
            UUID auctionId,
            String bidderName,
            BidStatus status
    );

    // leaderboard — top confirmed bids for an auction
    List<Bid> findByAuctionIdAndStatusOrderByWillingAmtSatsDesc(
            UUID auctionId,
            BidStatus status
    );

    // winner — highest confirmed bid when auction closes
    Optional<Bid> findTopByAuctionIdAndStatusOrderByWillingAmtSatsDescConfirmedAtAsc(
            UUID auctionId,
            BidStatus status
    );

    // expire all pending bids for an auction — called when host closes
    @Modifying
    @Query("""
            UPDATE Bid b SET b.status = 'EXPIRED'
            WHERE b.auction.id = :auctionId AND b.status = 'PENDING'
            """)
    void expireAllPendingBids(@Param("auctionId") UUID auctionId);

    // atomic confirmation — only updates if still PENDING
    // returns rows updated — 0 means already confirmed or expired
    @Modifying
    @Query("""
            UPDATE Bid b SET b.status = 'CONFIRMED', b.confirmedAt = :now
            WHERE b.paymentHash = :paymentHash AND b.status = 'PENDING'
            """)
    int confirmIfPending(
            @Param("paymentHash") String paymentHash,
            @Param("now") Instant now
    );

    // expiry scheduler — find all stale pending bids
    @Query("""
            SELECT b FROM Bid b
            WHERE b.status = 'PENDING' AND b.expiresAt < :now
            """)
    List<Bid> findExpiredPendingBids(@Param("now") Instant now);

    // polling — find all pending bids to check payment status
    @Query("SELECT b FROM Bid b WHERE b.status = 'PENDING'")
    List<Bid> findAllPending();
}
