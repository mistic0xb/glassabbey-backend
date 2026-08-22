package com.mist.glassabbey.piece;

import com.mist.glassabbey.auction.Auction;
import com.mist.glassabbey.gallery.Gallery;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "piece")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Piece {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gallery_id", nullable = false)
    private Gallery gallery;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "artist_name", nullable = false)
    private String artistName;

    @Column(name = "artist_profile", columnDefinition = "TEXT")
    private String artistProfile;

    @Column(name = "medium")
    private String medium;

    @Column(name = "dimensions")
    private String dimensions;

    @Column(name = "img_url")
    private String imgUrl;

    @Column(name = "base_price_sats")
    private Long basePriceSats;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    // one piece has one auction
    @OneToOne(
            mappedBy = "piece",
            cascade = CascadeType.ALL
    )
    private Auction auction;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
