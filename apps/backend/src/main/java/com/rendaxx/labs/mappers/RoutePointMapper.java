package com.rendaxx.labs.mappers;

import com.rendaxx.labs.domain.Order;
import com.rendaxx.labs.domain.RetailPoint;
import com.rendaxx.labs.domain.Route;
import com.rendaxx.labs.domain.RoutePoint;
import com.rendaxx.labs.dtos.RoutePointDto;
import com.rendaxx.labs.dtos.SaveRoutePointDto;
import com.rendaxx.labs.repository.view.RoutePointView;
import java.util.List;
import java.util.Set;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(uses = {OrderMapper.class, RetailPointMapper.class})
public abstract class RoutePointMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "plannedStartTime", source = "dto.plannedStartTime")
    @Mapping(target = "plannedEndTime", source = "dto.plannedEndTime")
    public abstract void update(
            @MappingTarget RoutePoint routePoint,
            SaveRoutePointDto dto,
            Route route,
            RetailPoint retailPoint,
            Set<Order> orders);

    @Mapping(target = "routeId", source = "routeId")
    public abstract RoutePointDto toDto(RoutePointView routePoint);

    public abstract List<RoutePointDto> toDtoFromView(List<RoutePointView> routePoints);
}
