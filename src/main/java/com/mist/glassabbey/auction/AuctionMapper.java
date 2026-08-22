package com.mist.glassabbey.auction;

import com.mist.glassabbey.auction.dtos.AuctionDto;
import com.mist.glassabbey.gallery.GalleryMapper;
import com.mist.glassabbey.piece.PieceMapper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {PieceMapper.class, GalleryMapper.class})
public interface AuctionMapper {
    AuctionDto toDto(Auction auction);
}
