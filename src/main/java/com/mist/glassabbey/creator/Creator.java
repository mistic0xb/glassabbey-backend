package com.mist.glassabbey.creator;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "creator")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Creator {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String pubkey;

    private String name;
    private String picture;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;
}
