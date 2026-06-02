package com.mist.glassabbey.piece;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PieceRepository extends JpaRepository<Piece, UUID> {
    List<Piece> findAllByGalleryId(UUID galleryId);

    Optional<Piece> findByIdAndGalleryId(UUID pieceId, UUID galleryId);
}
