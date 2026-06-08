package com.mist.glassabbey.payment;

import com.mist.glassabbey.bid.BidService;
import com.mist.glassabbey.payment.dtos.PaymentConfirmRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final BidService bidService;

    @PostMapping("/confirm")
    public ResponseEntity<Void> confirm(@RequestBody PaymentConfirmRequest request) {
        log.info("Manual payment confirmation: hash={}", request.paymentHash());
        bidService.confirmPayment(request.paymentHash());
        return ResponseEntity.ok().build();
    }
}
