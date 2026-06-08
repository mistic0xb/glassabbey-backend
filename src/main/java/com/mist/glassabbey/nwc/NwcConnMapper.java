package com.mist.glassabbey.nwc;

import com.mist.glassabbey.nwc.dtos.NwcConnDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NwcConnMapper {
    NwcConnDto toDto(NwcConn nwcConn);
}
