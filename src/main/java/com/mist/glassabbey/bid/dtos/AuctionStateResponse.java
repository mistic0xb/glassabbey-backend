package com.mist.glassabbey.bid.dtos;

import java.util.List;

public record AuctionStateResponse(
        String type,
        String auctionId,
        String pieceId,
        Long currentPrice,
        String status,
        List<BidDto> topBidders
) {}


