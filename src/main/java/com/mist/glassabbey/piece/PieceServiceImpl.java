package com.mist.glassabbey.piece;

import com.mist.glassabbey.exception.ForbiddenException;
import com.mist.glassabbey.gallery.Gallery;
import com.mist.glassabbey.gallery.GalleryRepository;
import com.mist.glassabbey.gallery.GalleryStatus;
import com.mist.glassabbey.piece.dtos.CreatePieceRequest;
import com.mist.glassabbey.piece.dtos.PieceDto;
import com.mist.glassabbey.piece.dtos.UpdatePieceRequest;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PieceServiceImpl implements PieceService {
    private final PieceRepository pieceRepository;
    private final GalleryRepository galleryRepository;
    private final PieceMapper pieceMapper;

    @Override
    @Transactional
    public PieceDto addPiece(UUID creatorId, UUID galleryId, CreatePieceRequest request) {
        Gallery existingGallery = galleryRepository.findByIdAndCreatorId(galleryId, creatorId)
                .orElseThrow(() -> new EntityNotFoundException("Gallery not found with id: " + galleryId));

        if (existingGallery.getStatus() != GalleryStatus.DRAFT) {
            throw new ForbiddenException("Cannot add pieces to a published or closed gallery");
        }

        Piece newPiece = Piece.builder()
                .gallery(existingGallery)
                .title(request.title())
                .description(request.description())
                .artistName(request.artistName())
                .artistProfile(request.artistProfile())
                .medium(request.medium())
                .dimensions(request.dimensions())
                .imgUrl(request.imgUrl())
                .basePriceSats(request.basePriceSats())
                .build();

        Piece saved = pieceRepository.save(newPiece);
        log.info("Piece added: id={}, galleryId={}", saved.getId(), galleryId);
        return pieceMapper.toDto(saved);
    }

    @Override
    public List<PieceDto> getAllPieces(UUID galleryId) {
        return pieceRepository.findAllByGalleryId(galleryId)
                .stream()
                .map(piece -> pieceMapper.toDto(piece)).toList();
    }

    @Override
    public PieceDto getPieceByIdAndGalleyId(UUID pieceId, UUID galleryId) {
        Piece piece = pieceRepository.findByIdAndGalleryId(pieceId, galleryId)
                .orElseThrow(() -> new EntityNotFoundException("Piece not found with id: " + pieceId));

        return pieceMapper.toDto(piece);
    }

    @Override
    @Transactional
    public PieceDto updatePiece(UUID creatorId, UUID galleryId, UUID pieceId, UpdatePieceRequest request) {
        Gallery gallery = galleryRepository.findByIdAndCreatorId(galleryId, creatorId)
                .orElseThrow(() -> new EntityNotFoundException("Gallery not found or not yours with galleryId: " + galleryId));
        if (gallery.getStatus() != GalleryStatus.DRAFT) {
            throw new ForbiddenException("Cannot delete pieces from a published or closed gallery");
        }

        Piece existingPiece = pieceRepository.findByIdAndGalleryId(pieceId, galleryId)
                .orElseThrow(() -> new EntityNotFoundException("Piece not found with id: " + pieceId));

        existingPiece.setTitle(request.title());
        existingPiece.setDescription(request.description());
        existingPiece.setArtistName(request.artistName());
        existingPiece.setArtistProfile(request.artistProfile());
        existingPiece.setMedium(request.medium());
        existingPiece.setDimensions(request.dimensions());
        existingPiece.setImgUrl(request.imgUrl());
        existingPiece.setBasePriceSats(request.basePriceSats());

        return pieceMapper.toDto(pieceRepository.save(existingPiece));
    }

    @Override
    public void deletePiece(UUID creatorId, UUID galleryId, UUID pieceId) {
        Gallery gallery = findOwnedGallery(creatorId, galleryId);
        if (gallery.getStatus() != GalleryStatus.DRAFT) {
            throw new ForbiddenException("Cannot delete pieces from a published or closed gallery");
        }

        Piece piece = pieceRepository.findByIdAndGalleryId(pieceId, galleryId)
                .orElseThrow(() -> new EntityNotFoundException("Piece not found with id: " + pieceId));

        pieceRepository.delete(piece);
        log.info("Piece deleted: id={}, galleryId={}", pieceId, galleryId);
    }

    private Gallery findOwnedGallery(UUID creatorId, UUID galleryId) {
        return galleryRepository.findByIdAndCreatorId(galleryId, creatorId)
                .orElseThrow(() -> new EntityNotFoundException("Gallery not found or not yours with galleryId: " + galleryId));
    }
}
