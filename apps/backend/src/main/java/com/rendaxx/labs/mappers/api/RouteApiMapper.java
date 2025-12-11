package com.rendaxx.labs.mappers.api;

import com.rendaxx.labs.api.v1.model.PageRouteApiDto;
import com.rendaxx.labs.api.v1.model.RouteApiDto;
import com.rendaxx.labs.api.v1.model.SaveRouteApiDto;
import com.rendaxx.labs.dtos.RouteDto;
import com.rendaxx.labs.dtos.SaveRouteDto;
import com.rendaxx.labs.mappers.api.support.PageSortMapper;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

@Mapper(uses = {RoutePointApiMapper.class, VehicleApiMapper.class})
public interface RouteApiMapper extends PageSortMapper {

    RouteApiDto toApi(RouteDto dto);

    List<RouteApiDto> toApi(List<RouteDto> dto);

    SaveRouteDto toDto(SaveRouteApiDto dto);

    @Mapping(target = "page", source = "number")
    @Mapping(target = "sort", expression = "java(toSortStrings(page.getSort()))")
    PageRouteApiDto toRoutePage(Page<RouteDto> page);
}
