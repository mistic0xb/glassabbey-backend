package com.mist.glassabbey.creator;


import com.mist.glassabbey.creator.dtos.CreatorDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CreatorMapper {
    CreatorDto toDto(Creator creator);
}
