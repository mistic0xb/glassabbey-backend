package com.mist.glassabbey.auction;

import com.mist.glassabbey.bid.Bid;
import com.mist.glassabbey.gallery.Gallery;
import com.mist.glassabbey.piece.Piece;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "auction")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Auction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // one auction → one piece
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "piece_id", nullable = false, unique = true)
    private Piece piece;

    // denormalized for efficient collection-level queries
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gallery_id", nullable = false)
    private Gallery gallery;

    @Column(name = "base_price_sats", nullable = false)
    private Long basePriceSats;

    @Column(name = "current_price_sats", nullable = false)
    private Long currentPriceSats;

    @Column(name = "submission_fee_sats", nullable = false)
    private Long submissionFeeSats;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuctionStatus status;

    @Version
    private Integer version;

    @Column(name = "closed_at")
    private Instant closedAt;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @OneToMany(
            mappedBy = "auction",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<Bid> bids = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        currentPriceSats = basePriceSats;
        if (status == null) status = AuctionStatus.OPEN;
    }
}