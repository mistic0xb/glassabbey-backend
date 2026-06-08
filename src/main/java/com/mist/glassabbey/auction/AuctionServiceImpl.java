package com.mist.glassabbey.auction;

import com.mist.glassabbey.auction.dtos.AuctionDto;
import com.mist.glassabbey.bid.BidRepository;
import com.mist.glassabbey.gallery.Gallery;
import com.mist.glassabbey.gallery.GalleryRepository;
import com.mist.glassabbey.piece.Piece;
import com.mist.glassabbey.piece.PieceRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionServiceImpl implements AuctionService {
    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final GalleryRepository galleryRepository;
    private final PieceRepository pieceRepository;
    private final AuctionMapper auctionMapper;
    private final long SUBMISSION_FEE = 100L;

    @Override
    public AuctionDto getByPieceId(UUID pieceId) {
        Auction auction = auctionRepository.findByPieceId(pieceId)
                .orElseThrow(() -> new EntityNotFoundException("Auction not found with pieceId: " + pieceId));

        return auctionMapper.toDto(auction);

    }

    @Override
    public AuctionDto closeSingle(UUID creatorId, UUID auctionId) {
        Auction auction = auctionRepository.findByIdAndCreatorId(auctionId, creatorId)
                .orElseThrow(() -> new EntityNotFoundException("Auction not found or not yours"));

        // expire all pending bids for this auction
        bidRepository.expireAllPendingBids(auctionId);

        auction.setStatus(AuctionStatus.CLOSED);
        auction.setClosedAt(Instant.now());

        Auction savedAuction = auctionRepository.save(auction);
        log.info("Auction closed: id={}, finalPrice={}", auctionId, auction.getCurrentPriceSats());

        return auctionMapper.toDto(savedAuction);
    }

    @Override
    @Transactional
    public void closeAllForGallery(UUID creatorId, UUID galleryId) {
        if (!galleryRepository.existsByIdAndCreatorId(galleryId, creatorId)) {
            throw new EntityNotFoundException("Gallery not found or not yours");
        }

        List<Auction> openAuctions = auctionRepository.findByGalleryIdAndStatus(galleryId, AuctionStatus.OPEN);

        // close all auctions
        openAuctions.forEach(auction -> {
            bidRepository.expireAllPendingBids(auction.getId());
            auction.setStatus(AuctionStatus.CLOSED);
            auction.setClosedAt(Instant.now());
        });

        auctionRepository.saveAll(openAuctions);
    }

    @Override
    @Transactional
    public void createForGallery(UUID galleryId) {
        Gallery gallery = galleryRepository.findById(galleryId)
                .orElseThrow(() -> new EntityNotFoundException("Gallery not found with id: " + galleryId));

        List<Piece> pieceList = pieceRepository.findAllByGalleryId(gallery.getId());

        List<Auction> auctionList = pieceList.stream()
                .map(piece -> Auction.builder()
                        .piece(piece)
                        .gallery(gallery)
                        .basePriceSats(piece.getBasePriceSats())
                        .currentPriceSats(piece.getBasePriceSats())
                        .submissionFeeSats(SUBMISSION_FEE)
                        .status(AuctionStatus.OPEN)
                        .build()
                ).toList();
        List<Auction> savedAuction = auctionRepository.saveAll(auctionList);
        log.info("Created {} auctions for gallery: {}", savedAuction.size(), galleryId);
    }
}