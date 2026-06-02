package com.mist.glassabbey.piece;


import com.mist.glassabbey.piece.dtos.CreatePieceRequest;
import com.mist.glassabbey.piece.dtos.PieceDto;
import com.mist.glassabbey.piece.dtos.UpdatePieceRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/gallery/{galleryId}/piece")
@RequiredArgsConstructor
public class PieceController {
    private final PieceService pieceService;

    @PostMapping
    public ResponseEntity<PieceDto> addPiece(
            @AuthenticationPrincipal UUID creatorId,
            @RequestBody CreatePieceRequest request,
            @PathVariable UUID galleryId
    ) {
        return ResponseEntity.ok()
                .body(pieceService.addPiece(creatorId, galleryId, request));
    }

    @GetMapping
    public ResponseEntity<List<PieceDto>> getAllPieces(
            @PathVariable UUID galleryId
    ) {
        List<PieceDto> pieceDtoList = pieceService.getAllPieces(galleryId);
        return ResponseEntity.ok().body(pieceDtoList);
    }

    @GetMapping("/{pieceId}")
    public ResponseEntity<PieceDto> getPiece(
            @PathVariable UUID pieceId,
            @PathVariable UUID galleryId
    ) {
        PieceDto pieceDto = pieceService.getPieceByIdAndGalleyId(pieceId, galleryId);
        return ResponseEntity.ok().body(pieceDto);
    }

    @PatchMapping("/{pieceId}")
    public ResponseEntity<PieceDto> updatePiece(
            @AuthenticationPrincipal UUID creatorId,
            @PathVariable UUID pieceId,
            @PathVariable UUID galleryId,
            @RequestBody UpdatePieceRequest request
    ) {
        PieceDto updatedPieceDto = pieceService.updatePiece(creatorId, galleryId, pieceId, request);
        return ResponseEntity.ok().body(updatedPieceDto);
    }

    @DeleteMapping("/{pieceId}")
    public ResponseEntity<Void> deletePiece(
            @AuthenticationPrincipal UUID creatorId,
            @PathVariable UUID pieceId,
            @PathVariable UUID galleryId
    ) {
        pieceService.deletePiece(creatorId, galleryId, pieceId);
        return ResponseEntity.noContent().build();
    }
}
