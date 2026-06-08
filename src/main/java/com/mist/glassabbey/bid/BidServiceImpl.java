package com.mist.glassabbey.bid;

import com.mist.glassabbey.auction.Auction;
import com.mist.glassabbey.auction.AuctionEventPublisher;
import com.mist.glassabbey.auction.AuctionRepository;
import com.mist.glassabbey.auction.AuctionStatus;
import com.mist.glassabbey.bid.dtos.BidAcceptedResponse;
import com.mist.glassabbey.bid.dtos.BidDto;
import com.mist.glassabbey.bid.dtos.SubmitBidRequest;
import com.mist.glassabbey.exception.BidRejectedException;
import com.mist.glassabbey.nwc.NwcConn;
import com.mist.glassabbey.nwc.NwcConnRepository;
import com.mist.glassabbey.nwc.NwcService;
import com.mist.glassabbey.nwc.dtos.MakeInvoiceResult;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BidServiceImpl implements BidService {
    private final BidRepository bidRepository;
    private final BidMapper bidMapper;
    private final AuctionRepository auctionRepository;
    private final NwcConnRepository nwcConnRepository;
    private final NwcService nwcService;
    private final AuctionEventPublisher eventPublisher;

    @Override
    @Transactional
    public BidAcceptedResponse submitBid(UUID pieceId, SubmitBidRequest request, String sessionId) {
        // 1. check idempotency
        Optional<Bid> existingBid = bidRepository.findByIdempotencyKey(request.idempotencyKey());
        if (existingBid.isPresent()) {
            log.info("[{}] Duplicate bid submission -> returning existing bid", pieceId);
            Bid bid = existingBid.get();
            return new BidAcceptedResponse(
                    bid.getId().toString(),
                    bid.getBidderName(),
                    bid.getWillingAmtSats(),
                    bid.getAuction().getSubmissionFeeSats(),
                    bid.getPaymentRequest(),
                    bid.getExpiresAt()
            );
        }

        // 2. lock auction row
        Auction auction = auctionRepository.findByPieceIdForUpdate(pieceId)
                .orElseThrow(() -> new EntityNotFoundException("Auction not found for piece: " + pieceId));

        // 3. validate auction is open
        if (auction.getStatus() != AuctionStatus.OPEN) {
            throw new BidRejectedException("Auction is closed");
        }

        // 4. validate one pending bid per bidder
        boolean hasPending = bidRepository.existsByAuctionIdAndBidderNameAndStatus(
                auction.getId(),
                request.bidderName(),
                BidStatus.PENDING
        );
        if (hasPending) {
            throw new BidRejectedException("You already have a bid in progress");
        }

        // 5. validate willing amt > current price
        long willingAmt = auction.getCurrentPriceSats() + request.bidIncrementSats();
        if (willingAmt <= auction.getCurrentPriceSats()) {
            throw new BidRejectedException("Bid increment must be positive");
        }

        // 6. get creator's primary nwc conn to generate invoice
        NwcConn nwcConn = nwcConnRepository
                .findPrimaryByGalleryCreatorId(auction.getGallery().getCreator().getId())
                .orElseThrow(() -> new BidRejectedException("Host has no payment method configured"));

        // 7. generate ln invoice via nwc
        MakeInvoiceResult makeInvoiceResult = nwcService.makeInvoice(
                nwcConn,
                auction.getSubmissionFeeSats(),
                "Bid fee: " + auction.getPiece().getTitle()
        );

        // 8. insert bid as PENDING
        Bid bid = Bid.builder()
                .auction(auction)
                .nwcConn(nwcConn)
                .sessionId(sessionId)
                .idempotencyKey(request.idempotencyKey())
                .bidderName(request.bidderName())
                .bidIncrementSats(request.bidIncrementSats())
                .willingAmtSats(willingAmt)
                .paymentHash(makeInvoiceResult.paymentHash())
                .paymentRequest(makeInvoiceResult.invoice())
                .status(BidStatus.PENDING)
                .build();
        bidRepository.save(bid);
        log.info("[{}] Bid accepted — bidder={}, willingAmt={}", pieceId, request.bidderName(), willingAmt);

        // 9. broadcast pending bid to room
        eventPublisher.publishBidPending(
                auction.getPiece().getId(),
                request.bidderName(),
                willingAmt
        );

        return new BidAcceptedResponse(
                bid.getId().toString(),
                bid.getBidderName(),
                willingAmt,
                auction.getSubmissionFeeSats(),
                makeInvoiceResult.invoice(),
                bid.getExpiresAt()
        );
    }

    @Override
    public void confirmPayment(String paymentHash) {

    }

    @Override
    public void cancelBid(UUID bidId, String sessionId) {

    }

    @Override
    public List<BidDto> getLeaderBoard(UUID auctionId) {
        return bidRepository
                .findByAuctionIdAndStatusOrderByWillingAmtSatsDesc(auctionId, BidStatus.PENDING) //TODO: change the confirmed on prod
                .stream()
                .map(bid -> bidMapper.toDto(bid))
                .toList();
    }
}
