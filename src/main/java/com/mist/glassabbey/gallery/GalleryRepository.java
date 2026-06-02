package com.mist.glassabbey.gallery;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GalleryRepository extends JpaRepository<Gallery, UUID> {
    @Query("SELECT g FROM Gallery g LEFT JOIN FETCH g.pieces")
    List<Gallery> findAllWithPieceCount();

    List<Gallery> findByCreatorIdOrderByCreatedAtDesc(UUID creatorId);

    Optional<Gallery> findByIdAndCreatorId(UUID galleryId,  UUID creatorId);
}
