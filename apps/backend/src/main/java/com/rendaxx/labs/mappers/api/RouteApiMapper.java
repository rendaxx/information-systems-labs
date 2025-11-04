package com.rendaxx.labs.mappers.api;

import com.rendaxx.labs.api.v1.model.RouteApiDto;
import com.rendaxx.labs.api.v1.model.SaveRouteApiDto;
import com.rendaxx.labs.dtos.RouteDto;
import com.rendaxx.labs.dtos.SaveRouteDto;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(uses = {RoutePointApiMapper.class, VehicleApiMapper.class})
public interface RouteApiMapper {

    RouteApiDto toApi(RouteDto dto);

    List<RouteApiDto> toApi(List<RouteDto> dto);

    SaveRouteDto toDto(SaveRouteApiDto dto);
}
