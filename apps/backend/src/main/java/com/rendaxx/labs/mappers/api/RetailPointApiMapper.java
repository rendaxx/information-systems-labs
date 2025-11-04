package com.rendaxx.labs.mappers.api;

import com.rendaxx.labs.api.v1.model.PageRetailPointApiDto;
import com.rendaxx.labs.api.v1.model.RetailPointApiDto;
import com.rendaxx.labs.api.v1.model.SaveRetailPointApiDto;
import com.rendaxx.labs.dtos.RetailPointDto;
import com.rendaxx.labs.dtos.SaveRetailPointDto;
import com.rendaxx.labs.mappers.api.support.PageSortMapper;
import com.rendaxx.labs.mappers.api.support.PointGeometryMapper;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

@Mapper(uses = {PointApiMapper.class, PointGeometryMapper.class})
public interface RetailPointApiMapper extends PageSortMapper {

    RetailPointApiDto toApi(RetailPointDto dto);

    List<RetailPointApiDto> toApi(List<RetailPointDto> dto);

    SaveRetailPointDto toDto(SaveRetailPointApiDto dto);

    @Mapping(target = "page", source = "number")
    PageRetailPointApiDto toRetailPointPage(Page<RetailPointDto> page);
}
