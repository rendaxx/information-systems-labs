package com.rendaxx.labs.mappers;

import com.rendaxx.labs.domain.Order;
import com.rendaxx.labs.domain.RetailPoint;
import com.rendaxx.labs.domain.Route;
import com.rendaxx.labs.domain.RoutePoint;
import com.rendaxx.labs.dtos.RoutePointDto;
import com.rendaxx.labs.dtos.SaveRoutePointDto;
import com.rendaxx.labs.exceptions.NotFoundException;
import com.rendaxx.labs.repository.OrderRepository;
import com.rendaxx.labs.repository.RetailPointRepository;
import com.rendaxx.labs.repository.RouteRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(uses = {OrderMapper.class, RetailPointMapper.class})
public abstract class RoutePointMapper {

    @Autowired
    private RouteRepository routeRepository;
    @Autowired
    private RetailPointRepository retailPointRepository;
    @Autowired
    private OrderRepository orderRepository;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "plannedStartTime", source = "dto.plannedStartTime")
    @Mapping(target = "plannedEndTime", source = "dto.plannedEndTime")
    abstract void update(
            @MappingTarget RoutePoint routePoint,
            SaveRoutePointDto dto,
            Route route,
            RetailPoint retailPoint,
            Set<Order> orders
    );

    public void update(RoutePoint routePoint, SaveRoutePointDto dto) {
        Route route = routeRepository
                .findById(dto.getRouteId())
                .orElseThrow(() -> new NotFoundException(Route.class, dto.getRouteId()));
        RetailPoint retailPoint = retailPointRepository
                .findById(dto.getRetailPointId())
                .orElseThrow(() -> new NotFoundException(RetailPoint.class, dto.getRetailPointId()));
        Set<Order> orders = new HashSet<>(orderRepository.findAllById(dto.getOrderIds()));
        update(routePoint, dto, route, retailPoint, orders);
    }

    @Mapping(target = "routeId", source = "route.id")
    public abstract RoutePointDto toDto(RoutePoint routePoint);

    public abstract List<RoutePointDto> toDto(List<RoutePoint> routePoints);
}
