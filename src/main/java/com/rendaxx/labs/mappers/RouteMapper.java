package com.rendaxx.labs.mappers;

import com.rendaxx.labs.domain.Route;
import com.rendaxx.labs.domain.RoutePoint;
import com.rendaxx.labs.domain.Vehicle;
import com.rendaxx.labs.dtos.SaveRouteDto;
import com.rendaxx.labs.dtos.SaveRoutePointDto;
import com.rendaxx.labs.exceptions.NotFoundException;
import com.rendaxx.labs.repository.RoutePointRepository;
import com.rendaxx.labs.repository.VehicleRepository;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Mapper(uses = RoutePointMapper.class)
public abstract class RouteMapper {

    @Autowired
    private RoutePointRepository routePointRepository;
    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private RoutePointMapper routePointMapper;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationTime", ignore = true)
    @Mapping(target = "routePoints", source = "routePoints")
    public abstract void update(
            @MappingTarget Route route,
            SaveRouteDto dto,
            List<RoutePoint> routePoints,
            Vehicle vehicle
    );

    public void update(Route route, SaveRouteDto dto) {
        Map<Long, RoutePoint> routePointsById = routePointRepository
                .findAllById(
                        dto.getRoutePoints().stream()
                                .map(SaveRoutePointDto::getId)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList())
                )
                .stream()
                .collect(Collectors.toMap(RoutePoint::getId, Function.identity()));
        List<RoutePoint> routePoints = new ArrayList<>();
        for (SaveRoutePointDto routePointDto : dto.getRoutePoints()) {
            RoutePoint routePoint = routePointsById.getOrDefault(routePointDto.getId(), new RoutePoint());
            routePointMapper.update(routePoint, routePointDto);
            routePoints.add(routePoint);
        }

        Vehicle vehicle = vehicleRepository
                .findById(dto.getVehicleId())
                .orElseThrow(() -> new NotFoundException(Vehicle.class, dto.getVehicleId()));

        update(route, dto, routePoints, vehicle);
    }
}
