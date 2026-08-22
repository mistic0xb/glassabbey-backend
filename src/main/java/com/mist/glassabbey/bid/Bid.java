package com.mist.glassabbey.bid;

import com.mist.glassabbey.auction.Auction;
import com.mist.glassabbey.nwc.NwcConn;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "bid")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Bid {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_id", nullable = false)
    private Auction auction;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "nwc_conn_id", nullable = false)
    private NwcConn nwcConn;

    @Column(name = "session_id")
    private String sessionId;

    @Column(name = "idempotency_key", nullable = false, unique = true)
    private UUID idempotencyKey;

    @Column(name = "bidder_name", nullable = false)
    private String bidderName;

    @Column(name = "bid_increment_sats", nullable = false)
    private Long bidIncrementSats;

    @Column(name = "willing_amt_sats", nullable = false)
    private Long willingAmtSats;

    @Column(name = "payment_hash", unique = true)
    private String paymentHash;

    @Column(name = "payment_request", columnDefinition = "TEXT")
    private String paymentRequest;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BidStatus status;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "confirmed_at")
    private Instant confirmedAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        if (status == null) status = BidStatus.PENDING;
        if (expiresAt == null) expiresAt = createdAt.plusSeconds(120);
    }
}