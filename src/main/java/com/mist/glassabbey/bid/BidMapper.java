package com.mist.glassabbey.bid;

import com.mist.glassabbey.bid.dtos.BidDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BidMapper {
    BidDto toDto(Bid bid);
}
