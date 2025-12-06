package com.rendaxx.labs.mappers;

import com.rendaxx.labs.domain.Route;
import com.rendaxx.labs.domain.RoutePoint;
import com.rendaxx.labs.domain.Vehicle;
import com.rendaxx.labs.dtos.RouteDto;
import com.rendaxx.labs.dtos.SaveRouteDto;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(uses = {RoutePointMapper.class, VehicleMapper.class})
public abstract class RouteMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationTime", ignore = true)
    @Mapping(target = "routePoints", source = "routePoints")
    public abstract void update(
            @MappingTarget Route route, SaveRouteDto dto, List<RoutePoint> routePoints, Vehicle vehicle);

    public abstract RouteDto toDto(Route route);

    public abstract List<RouteDto> toDto(List<Route> routes);
}
