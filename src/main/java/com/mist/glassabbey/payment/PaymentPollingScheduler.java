package com.mist.glassabbey.payment;

import com.mist.glassabbey.bid.Bid;
import com.mist.glassabbey.bid.BidRepository;
import com.mist.glassabbey.bid.BidService;
import com.mist.glassabbey.nwc.NwcService;
import com.mist.glassabbey.nwc.dtos.InvoiceStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentPollingScheduler {

    private final BidRepository bidRepository;
    private final BidService bidService;
    private final NwcService nwcService;

    @Scheduled(fixedDelay = 5000) // every 5 seconds
    @Transactional
    public void pollPendingPayments() {
        List<Bid> pendingBids = bidRepository.findAllPending();
        if (pendingBids.isEmpty()) return;

        log.debug("Polling {} pending bids", Optional.of(pendingBids.size()));

        for (Bid bid : pendingBids) {
            try {
                InvoiceStatus status = nwcService.lookupInvoice(
                        bid.getNwcConn(),
                        bid.getPaymentHash()
                );

                if (status == InvoiceStatus.SETTLED) {
                    log.info("Payment settled via polling: hash={}, bidder={}",
                            bid.getPaymentHash(), bid.getBidderName());
                    bidService.confirmPayment(bid.getPaymentHash());
                }
                // PENDING / UNKNOWN → do nothing, retry next tick
                // EXPIRED / FAILED → bid expiry scheduler handles cleanup

            } catch (Exception e) {
                // never let one failure stop the rest
                log.warn("Poll failed for bid={}: {}", bid.getId(), e.getMessage());
            }
        }
    }
}
