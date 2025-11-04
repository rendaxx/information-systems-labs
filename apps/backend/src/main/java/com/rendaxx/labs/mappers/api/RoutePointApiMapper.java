package com.rendaxx.labs.mappers.api;

import com.rendaxx.labs.api.v1.model.RoutePointApiDto;
import com.rendaxx.labs.api.v1.model.SaveRoutePointApiDto;
import com.rendaxx.labs.dtos.RoutePointDto;
import com.rendaxx.labs.dtos.SaveRoutePointDto;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.openapitools.jackson.nullable.JsonNullable;

@Mapper(uses = {RetailPointApiMapper.class, OrderApiMapper.class})
public interface RoutePointApiMapper {

    RoutePointApiDto toApi(RoutePointDto dto);

    List<RoutePointApiDto> toApi(List<RoutePointDto> dto);

    @Mapping(target = "id", expression = "java(fromNullable(dto.getId()))")
    SaveRoutePointDto toDto(SaveRoutePointApiDto dto);

    default Long fromNullable(JsonNullable<Long> value) {
        if (value == null || !value.isPresent()) {
            return null;
        }
        return value.orElse(null);
    }

    default JsonNullable<Long> toNullable(Long value) {
        if (value == null) {
            return JsonNullable.undefined();
        }
        return JsonNullable.of(value);
    }
}
