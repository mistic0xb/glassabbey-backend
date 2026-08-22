package com.mist.glassabbey.bid;

import com.mist.glassabbey.bid.dtos.BidAcceptedResponse;
import com.mist.glassabbey.bid.dtos.BidDto;
import com.mist.glassabbey.bid.dtos.SubmitBidRequest;

import java.util.List;
import java.util.UUID;

public interface BidService {
    BidAcceptedResponse submitBid(UUID pieceId, SubmitBidRequest request, String sessionId);

    void confirmPayment(String paymentHash);

    boolean isPaymentConfirmed(UUID bidId);

    void cancelBid(UUID bidId, String sessionId);

    List<BidDto> getLeaderBoard(UUID auctionId);
}
