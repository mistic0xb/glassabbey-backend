package com.mist.glassabbey.gallery;

import com.mist.glassabbey.auction.AuctionService;
import com.mist.glassabbey.creator.Creator;
import com.mist.glassabbey.creator.CreatorRepository;
import com.mist.glassabbey.exception.ForbiddenException;
import com.mist.glassabbey.gallery.dtos.CreateGalleryRequest;
import com.mist.glassabbey.gallery.dtos.GalleryDto;
import com.mist.glassabbey.gallery.dtos.UpdateGalleryRequest;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GalleryServiceImpl implements GalleryService {
    private final GalleryRepository galleryRepository;
    private final CreatorRepository creatorRepository;
    private final GalleryMapper galleryMapper;
    private final AuctionService auctionService;

    @Override
    public Gallery create(UUID creatorId, CreateGalleryRequest request) {
        Creator creator = creatorRepository.findById(creatorId)
                .orElseThrow(() -> new EntityNotFoundException("Creator not found with ID: " + creatorId));

        Gallery newGallery = Gallery.builder()
                .creator(creator)
                .title(request.title())
                .description(request.description())
                .coverImageUrl(request.coverImageUrl())
                .endAt(request.endAt())
                .status(GalleryStatus.DRAFT)
                .build();

        Gallery savedGallery = galleryRepository.save(newGallery);
        log.info("Gallery created: galleryId={}, creatorId={}", savedGallery.getId(), creator.getId());
        return savedGallery;
    }

    @Override
    public List<GalleryDto> getCreatorGalleriesByCreatorId(UUID creatorId) {
        return galleryRepository
                .findByCreatorIdOrderByCreatedAtDesc(creatorId)
                .stream()
                .map(gallery -> galleryMapper.toDto(gallery))
                .toList();
    }

    @Override
    public List<GalleryDto> getAllPublishedGalleries() {
        return galleryRepository
                .findAllWithPieceCount()
                .stream()
                .map(gallery -> galleryMapper.toDto(gallery))
                .toList();
    }

    @Override
    public GalleryDto getById(UUID galleryId) {
        Gallery gallery = galleryRepository.findById(galleryId)
                .orElseThrow(() -> new EntityNotFoundException("Gallery not found with galleryId: " + galleryId));

        return galleryMapper.toDto(gallery);
    }

    @Override
    @Transactional
    public GalleryDto update(UUID creatorId, UUID galleryId, UpdateGalleryRequest request) {
        Gallery existingGallery = galleryRepository.findByIdAndCreatorId(galleryId, creatorId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Gallery not found with galleryId=$s and creatorId=%s"
                                .formatted(galleryId, creatorId)
                ));

        if (existingGallery.getStatus() != GalleryStatus.DRAFT) {
            throw new ForbiddenException("Only DRAFT galleries can be updated");
        }

        existingGallery.setTitle(request.title());
        existingGallery.setDescription(request.description());
        existingGallery.setCoverImageUrl(request.coverImageUrl());
        existingGallery.setEndAt(request.endAt());
        existingGallery.setUpdatedAt(Instant.now());

        Gallery savedGallery = galleryRepository.save(existingGallery);
        log.info("Gallery updated: galleryId={}, creatorId={}", savedGallery.getId(), creatorId);
        return galleryMapper.toDto(savedGallery);
    }

    @Override
    public GalleryDto publish(UUID creatorId, UUID galleryId) {
        Gallery existingGallery = galleryRepository.findByIdAndCreatorId(galleryId, creatorId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Gallery not found with galleryId=$s and creatorId=%s"
                                .formatted(galleryId, creatorId)
                ));

        if (existingGallery.getStatus() != GalleryStatus.DRAFT) {
            throw new ForbiddenException("Only DRAFT galleries can be updated");
        }

        if (existingGallery.getPieces().isEmpty()) {
            throw new ForbiddenException("Cannot publish a gallery with no pieces");
        }

        existingGallery.setStatus(GalleryStatus.PUBLISHED);
        existingGallery.setPublishedAt(Instant.now());

        // TODO: create auctions for each piece here
         auctionService.createForGallery(existingGallery.getId());

        log.info("Gallery published: galleryId={}", galleryId);
        return galleryMapper.toDto(galleryRepository.save(existingGallery));
    }

    @Override
    public GalleryDto close(UUID creatorId, UUID galleryId) {
        Gallery existingGallery = galleryRepository.findByIdAndCreatorId(galleryId, creatorId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Gallery not found with galleryId=$s and creatorId=%s"
                                .formatted(galleryId, creatorId)
                ));

        if (existingGallery.getStatus() == GalleryStatus.CLOSED) {
            return galleryMapper.toDto(existingGallery);
        }

        existingGallery.setStatus(GalleryStatus.CLOSED);
        existingGallery.setEndAt(Instant.now());

        // TODO: close all open auction for this gallery
        // auctionService.closeAllForGallery(galleryId);


        Gallery savedGallery = galleryRepository.save(existingGallery);
        log.info("Gallery closed: galleryId={}, creatorId={}", savedGallery.getId(), creatorId);
        return galleryMapper.toDto(savedGallery);

    }
}
