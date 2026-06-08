package com.mist.glassabbey.auction;

import com.mist.glassabbey.auction.dtos.AuctionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/auction")
public class AuctionController {
    private final AuctionService auctionService;

    @GetMapping("/piece/{pieceId}")
    public ResponseEntity<AuctionDto> getAuction(
            @PathVariable UUID pieceId
    ) {
        return ResponseEntity.ok().body(auctionService.getByPieceId(pieceId));
    }

    @PostMapping("/{auctionId}/close")
    public ResponseEntity<AuctionDto> closeSingle(
            @AuthenticationPrincipal UUID creatorId,
            @PathVariable UUID auctionId
    ) {
        return ResponseEntity.ok().body(auctionService.closeSingle(creatorId, auctionId));
    }

    @PostMapping("/gallery/{galleryId}/close-all")
    public ResponseEntity<Void> closeAll(
            @AuthenticationPrincipal UUID creatorId,
            @PathVariable UUID galleryId
    ) {
        auctionService.closeAllForGallery(creatorId, galleryId);
        return ResponseEntity.noContent().build();
    }
}