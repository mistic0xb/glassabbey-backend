package com.mist.glassabbey.piece;

import com.mist.glassabbey.piece.dtos.CreatePieceRequest;
import com.mist.glassabbey.piece.dtos.PieceDto;
import com.mist.glassabbey.piece.dtos.UpdatePieceRequest;

import java.util.List;
import java.util.UUID;

public interface PieceService {
    PieceDto addPiece(UUID creatorId, UUID galleryId, CreatePieceRequest request);

    List<PieceDto> getAllPieces(UUID galleryId);

    PieceDto getPieceByIdAndGalleyId(UUID pieceId, UUID galleryId);

    PieceDto updatePiece(UUID creatorId, UUID galleryId, UUID pieceId, UpdatePieceRequest request);

    void deletePiece(UUID creatorId, UUID galleryId, UUID pieceId);
}
