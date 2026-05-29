package com.mist.glassabbey.gallery;

import com.mist.glassabbey.gallery.dtos.CreateGalleryRequest;
import com.mist.glassabbey.gallery.dtos.GalleryDto;
import com.mist.glassabbey.gallery.dtos.UpdateGalleryRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(path = "/api/v1/gallery")
@RequiredArgsConstructor
public class GalleryController {
    private final GalleryService galleryService;
    private final GalleryMapper galleryMapper;

    @PostMapping
    public ResponseEntity<GalleryDto> create(
            @AuthenticationPrincipal UUID creatorId,
            @Valid @RequestBody CreateGalleryRequest request
    ) {
        Gallery gallery = galleryService.create(creatorId, request);
        GalleryDto galleryDto = galleryMapper.toDto(gallery);

        return ResponseEntity.status(HttpStatus.CREATED).body(galleryDto);
    }

    @GetMapping("/my")
    public ResponseEntity<List<GalleryDto>> getCreatorGalleries(
            @AuthenticationPrincipal UUID creatorId
    ) {
        List<GalleryDto> creatorGalleriesDtos = galleryService.getCreatorGalleriesByCreatorId(creatorId);

        return ResponseEntity.ok().body(creatorGalleriesDtos);
    }


    // explore page
    @GetMapping
    public ResponseEntity<List<GalleryDto>> getPublishedGalleries(
    ) {
        List<GalleryDto> galleryDtos = galleryService.getAllPublishedGalleries();
        return ResponseEntity.ok().body(galleryDtos);
    }

    @GetMapping("/{galleryId}")
    public ResponseEntity<GalleryDto> getById(
            @PathVariable UUID galleryId
    ) {
        GalleryDto galleryDto = galleryService.getById(galleryId);
        return ResponseEntity.ok().body(galleryDto);
    }

    @PutMapping("/{galleryId}")
    public ResponseEntity<GalleryDto> update(
            @AuthenticationPrincipal UUID creatorId,
            @PathVariable UUID galleryId,
            @Valid @RequestBody UpdateGalleryRequest request
    ) {
        GalleryDto updatedGallery = galleryService.update(creatorId, galleryId, request);
        return ResponseEntity.ok().body(updatedGallery);
    }

    @PostMapping("/{galleryId}/publish")
    public ResponseEntity<GalleryDto> publish(
            @AuthenticationPrincipal UUID creatorId,
            @PathVariable UUID galleryId
    ) {
        GalleryDto publisedGalleryDto = galleryService.publish(creatorId, galleryId);
        return ResponseEntity.ok().body(publisedGalleryDto);
    }

    @PostMapping("/{galleryId}/close")
    public ResponseEntity<GalleryDto> close(
            @AuthenticationPrincipal UUID creatorId,
            @PathVariable UUID galleryId
    ) {
        GalleryDto galleryDto = galleryService.close(creatorId, galleryId);
        return ResponseEntity.ok().body(galleryDto);
    }
}
