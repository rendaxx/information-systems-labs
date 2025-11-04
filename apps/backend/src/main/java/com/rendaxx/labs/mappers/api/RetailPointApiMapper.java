package com.rendaxx.labs.mappers.api;

import com.rendaxx.labs.api.v1.model.RetailPointApiDto;
import com.rendaxx.labs.api.v1.model.SaveRetailPointApiDto;
import com.rendaxx.labs.dtos.RetailPointDto;
import com.rendaxx.labs.dtos.SaveRetailPointDto;
import com.rendaxx.labs.mappers.api.support.PointGeometryMapper;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {PointApiMapper.class, PointGeometryMapper.class})
public interface RetailPointApiMapper {

    RetailPointApiDto toApi(RetailPointDto dto);

    List<RetailPointApiDto> toApi(List<RetailPointDto> dto);

    @Mapping(target = "location", source = "location")
    SaveRetailPointDto toDto(SaveRetailPointApiDto dto);
}
