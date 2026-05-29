package com.mist.glassabbey.gallery;

import com.mist.glassabbey.gallery.dtos.GalleryDto;
import com.mist.glassabbey.piece.Piece;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.Set;


@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface GalleryMapper {
    @Mapping(target = "id", source = "id")
    @Mapping(target = "creatorId", source = "creator.id")
    @Mapping(target = "creatorName", source = "creator.name")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "pieceCount", source = "pieces", qualifiedByName = "calculatePieceCount")
    GalleryDto toDto(Gallery gallery);


    @Named("calculatePieceCount")
    default int calculatePostCount(List<Piece> pieces) {
        if (pieces == null) {
            return 0;
        }
        return pieces.size();
    }
}
