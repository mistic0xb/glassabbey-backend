package com.mist.glassabbey.auction;

import com.mist.glassabbey.auction.dtos.AuctionDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AuctionMapper {
    @Mapping(target = "pieceId", source = "piece.id")
    @Mapping(target = "galleryId", source = "gallery.id")
    @Mapping(target = "status", source = "status")
    AuctionDto toDto(Auction auction);
}
