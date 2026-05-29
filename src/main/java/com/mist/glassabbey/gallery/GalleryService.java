package com.mist.glassabbey.gallery;


import com.mist.glassabbey.gallery.dtos.CreateGalleryRequest;
import com.mist.glassabbey.gallery.dtos.GalleryDto;
import com.mist.glassabbey.gallery.dtos.UpdateGalleryRequest;

import java.util.List;
import java.util.UUID;

public interface GalleryService {
    Gallery create(UUID creatorId, CreateGalleryRequest request);

    List<GalleryDto> getCreatorGalleriesByCreatorId(UUID creatorId);

    List<GalleryDto> getAllPublishedGalleries();

    GalleryDto getById(UUID galleryId);

    GalleryDto update(UUID creatorId, UUID galleryId, UpdateGalleryRequest request);

    GalleryDto publish(UUID creatorId, UUID galleryId);

    GalleryDto close(UUID creatorId, UUID galleryId);
}
