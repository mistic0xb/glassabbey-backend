package com.mist.glassabbey.bid;

import com.mist.glassabbey.bid.dtos.BidDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/auction/{auctionId}/bid")
@RequiredArgsConstructor
public class BidController {
    private final BidService bidService;

    @GetMapping
    public ResponseEntity<List<BidDto>> getLeaderBoard(
            @PathVariable UUID auctionId
    ) {
        return ResponseEntity.ok().body(bidService.getLeaderBoard(auctionId));
    }

    @GetMapping("/check/{bidId}")
    public ResponseEntity<Map<String, Boolean>> checkPayment(
            @PathVariable UUID bidId
    ) {
        boolean paid = bidService.isPaymentConfirmed(bidId);
        return ResponseEntity.ok(Map.of("paid", paid));
    }
}
