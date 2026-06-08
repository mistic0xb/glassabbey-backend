package com.mist.glassabbey.auction;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, UUID> {
    Optional<Auction> findByPieceId(UUID pieceId);

    List<Auction> findByGalleryIdAndStatus(UUID galleryId, AuctionStatus auctionStatus);

    @Query("""
            SELECT a FROM Auction a WHERE a.id = :auctionId AND a.gallery.creator.id = :creatorId
            """)
    Optional<Auction> findByIdAndCreatorId(
            @Param("auctionId") UUID auctionId,
            @Param("creatorId") UUID creatorId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Auction a WHERE a.piece.id = :pieceId")
    Optional<Auction> findByPieceIdForUpdate(UUID pieceId);

    @Modifying
    @Query("""
            UPDATE Auction a
            SET a.currentPriceSats = :newPrice, a.version = a.version + 1
            WHERE a.id = :auctionId AND a.currentPriceSats < :newPrice
            """)
    int updatePriceIfHigher(
            @Param("auctionId") UUID auctionId,
            @Param("newPrice") long newPrice
    );
}