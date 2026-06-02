package com.mist.glassabbey.piece;

import com.mist.glassabbey.piece.dtos.PieceDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PieceMapper {

    @Mapping(target = "galleryId", source = "gallery.id")
    PieceDto toDto(Piece creator);
}
