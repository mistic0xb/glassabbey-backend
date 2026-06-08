package com.mist.glassabbey.nwc;

import com.mist.glassabbey.creator.Creator;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "nwc_conn")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NwcConn {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private Creator creator;

    @Column(name = "wallet_name", nullable = false)
    private String walletName;

    @Column(name = "encrypted_nwc", nullable = false, columnDefinition = "TEXT")
    private String encryptedNwc;

    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "last_used_at")
    private Instant lastUsedAt;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        if (isActive == null) isActive = true;
        if (isPrimary == null) isPrimary = false;
    }
}
