package com.mist.glassabbey.auction;

import com.mist.glassabbey.auction.dtos.AuctionDto;

import java.util.UUID;

public interface AuctionService {
    void createForGallery(UUID galleryId);

    AuctionDto getByPieceId(UUID auctionId);

    AuctionDto closeSingle(UUID creatorId, UUID auctionId);

    void closeAllForGallery(UUID creatorId, UUID galleryId);
}