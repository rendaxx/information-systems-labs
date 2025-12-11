package com.rendaxx.labs.mappers.api;

import com.rendaxx.labs.api.v1.model.PageRoutePointApiDto;
import com.rendaxx.labs.api.v1.model.RoutePointApiDto;
import com.rendaxx.labs.api.v1.model.SaveRoutePointApiDto;
import com.rendaxx.labs.dtos.RoutePointDto;
import com.rendaxx.labs.dtos.SaveRoutePointDto;
import com.rendaxx.labs.mappers.api.support.JsonNullableMapper;
import com.rendaxx.labs.mappers.api.support.PageSortMapper;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

@Mapper(uses = {RetailPointApiMapper.class, OrderApiMapper.class})
public interface RoutePointApiMapper extends JsonNullableMapper, PageSortMapper {

    RoutePointApiDto toApi(RoutePointDto dto);

    List<RoutePointApiDto> toApi(List<RoutePointDto> dto);

    SaveRoutePointDto toDto(SaveRoutePointApiDto dto);

    @Mapping(target = "page", source = "number")
    @Mapping(target = "sort", expression = "java(toSortStrings(page.getSort()))")
    PageRoutePointApiDto toRoutePointPage(Page<RoutePointDto> page);
}
