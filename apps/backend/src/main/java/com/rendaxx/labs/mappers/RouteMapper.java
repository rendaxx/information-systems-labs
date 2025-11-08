package com.rendaxx.labs.mappers;

import com.rendaxx.labs.domain.Route;
import com.rendaxx.labs.domain.RoutePoint;
import com.rendaxx.labs.domain.Vehicle;
import com.rendaxx.labs.dtos.RouteDto;
import com.rendaxx.labs.dtos.SaveRouteDto;
import com.rendaxx.labs.dtos.SaveRoutePointDto;
import com.rendaxx.labs.exceptions.NotFoundException;
import com.rendaxx.labs.repository.RoutePointRepository;
import com.rendaxx.labs.repository.VehicleRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(uses = {RoutePointMapper.class, VehicleMapper.class})
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
    abstract void update(@MappingTarget Route route, SaveRouteDto dto, List<RoutePoint> routePoints, Vehicle vehicle);

    public void update(Route route, SaveRouteDto dto) {
        List<SaveRoutePointDto> incomingRoutePoints = dto.getRoutePoints() == null
                ? List.of()
                : dto.getRoutePoints().stream().filter(Objects::nonNull).toList();

        Map<Long, RoutePoint> routePointsById = routePointRepository
                .findAllById(incomingRoutePoints.stream()
                        .map(SaveRoutePointDto::getId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()))
                .stream()
                .collect(Collectors.toMap(RoutePoint::getId, Function.identity()));
        List<RoutePoint> routePoints = new ArrayList<>();
        for (SaveRoutePointDto routePointDto : incomingRoutePoints) {
            RoutePoint routePoint = routePointsById.getOrDefault(routePointDto.getId(), new RoutePoint());
            routePointMapper.update(routePoint, routePointDto, route);
            routePoints.add(routePoint);
        }

        Vehicle vehicle = vehicleRepository
                .findById(dto.getVehicleId())
                .orElseThrow(() -> new NotFoundException(Vehicle.class, dto.getVehicleId()));

        update(route, dto, routePoints, vehicle);
    }

    public abstract RouteDto toDto(Route route);

    public abstract List<RouteDto> toDto(List<Route> routes);
}
