package com.rendaxx.labs.service;

import com.rendaxx.labs.domain.Order;
import com.rendaxx.labs.domain.RetailPoint;
import com.rendaxx.labs.domain.Route;
import com.rendaxx.labs.domain.RoutePoint;
import com.rendaxx.labs.domain.Vehicle;
import com.rendaxx.labs.dtos.RouteDto;
import com.rendaxx.labs.dtos.SaveRouteDto;
import com.rendaxx.labs.dtos.SaveRoutePointDto;
import com.rendaxx.labs.events.EntityChangePublisher;
import com.rendaxx.labs.events.EntityChangeType;
import com.rendaxx.labs.exceptions.BadRequestException;
import com.rendaxx.labs.exceptions.NotFoundException;
import com.rendaxx.labs.mappers.RouteMapper;
import com.rendaxx.labs.mappers.RoutePointMapper;
import com.rendaxx.labs.repository.OrderRepository;
import com.rendaxx.labs.repository.RetailPointRepository;
import com.rendaxx.labs.repository.RoutePointRepository;
import com.rendaxx.labs.repository.RouteRepository;
import com.rendaxx.labs.repository.VehicleRepository;
import com.rendaxx.labs.repository.support.RepositoryGuard;
import com.rendaxx.labs.service.specification.EqualitySpecificationBuilder;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class RouteService {

    private static final int MILEAGE_SCALE = 3;

    RouteMapper mapper;
    RoutePointMapper routePointMapper;
    RouteRepository repository;
    RoutePointRepository routePointRepository;
    VehicleRepository vehicleRepository;
    RetailPointRepository retailPointRepository;
    OrderRepository orderRepository;
    EntityChangePublisher changePublisher;
    EqualitySpecificationBuilder specificationBuilder;
    RepositoryGuard repositoryGuard;

    private static final String DESTINATION = "/topic/routes";

    public RouteDto create(SaveRouteDto command) {
        Route route = save(command, new Route());
        RouteDto dto = mapper.toDto(route);
        changePublisher.publish(DESTINATION, Objects.requireNonNull(route.getId()), dto, EntityChangeType.CREATED);
        return dto;
    }

    @Transactional(readOnly = true)
    public RouteDto getById(Long id) {
        Route route = repositoryGuard.execute(
                () -> repository.findById(id).orElseThrow(() -> new NotFoundException(Route.class, id)));
        return mapper.toDto(route);
    }

    @Transactional(readOnly = true)
    public Page<RouteDto> getAll(Pageable pageable, Map<String, String> filters) {
        Specification<Route> specification = specificationBuilder.build(filters);
        Page<Route> result = repositoryGuard.execute(() -> repository.findAll(specification, pageable));
        return result.map(mapper::toDto);
    }

    public RouteDto update(Long id, SaveRouteDto command) {
        Route route = repositoryGuard.execute(
                () -> repository.findById(id).orElseThrow(() -> new NotFoundException(Route.class, id)));
        Route savedRoute = save(command, route);
        RouteDto dto = mapper.toDto(savedRoute);
        changePublisher.publish(DESTINATION, Objects.requireNonNull(savedRoute.getId()), dto, EntityChangeType.UPDATED);
        return dto;
    }

    public void delete(Long id) {
        Route route = repositoryGuard.execute(
                () -> repository.findById(id).orElseThrow(() -> new NotFoundException(Route.class, id)));
        repositoryGuard.execute(() -> repository.delete(route));
        changePublisher.publish(DESTINATION, Objects.requireNonNull(route.getId()), null, EntityChangeType.DELETED);
    }

    public void recalculateRoutePointOrderNumber(Route route) {
        for (int i = 0; i < route.getRoutePoints().size(); i++) {
            route.getRoutePoints().get(i).setOrderNumber(i);
        }
    }

    @Transactional(readOnly = true)
    public BigDecimal getAverageMileageInKm() {
        BigDecimal averageMileage = repositoryGuard.execute(repository::findAverageMileageInKm);
        return averageMileage.setScale(MILEAGE_SCALE, RoundingMode.HALF_UP);
    }

    @Transactional(readOnly = true)
    public List<RouteDto> getWithinPeriod(LocalDateTime periodStart, LocalDateTime periodEnd) {
        if (periodStart.isAfter(periodEnd)) {
            throw new BadRequestException("Period start must not be after period end");
        }
        List<Route> routes =
                repositoryGuard.execute(() -> repository.findWithinPeriodWithDetails(periodStart, periodEnd));
        return mapper.toDto(routes);
    }

    @Transactional(readOnly = true)
    public List<RouteDto> getByRetailPointId(Long retailPointId) {
        List<Route> routes = repositoryGuard.execute(() -> repository.findAllByRetailPointId(retailPointId));
        return mapper.toDto(routes);
    }

    private Route save(SaveRouteDto command, Route route) {
        Vehicle vehicle = resolveVehicle(command.getVehicleId());
        List<RoutePoint> routePoints = mapRoutePoints(command, route);
        mapper.update(route, command, routePoints, vehicle);
        return repositoryGuard.execute(() -> repository.save(route));
    }

    private Vehicle resolveVehicle(Long vehicleId) {
        return repositoryGuard.execute(() -> vehicleRepository
                .findById(vehicleId)
                .orElseThrow(() -> new NotFoundException(Vehicle.class, vehicleId)));
    }

    private List<RoutePoint> mapRoutePoints(SaveRouteDto command, Route route) {
        List<SaveRoutePointDto> routePointsDto = command.getRoutePoints();
        if (routePointsDto.isEmpty()) {
            return List.of();
        }

        List<Long> existingIds = routePointsDto.stream()
                .map(SaveRoutePointDto::getId)
                .filter(Objects::nonNull)
                .toList();
        Map<Long, RoutePoint> routePointsById =
                repositoryGuard.execute(() -> routePointRepository.findAllById(existingIds)).stream()
                        .collect(Collectors.toMap(RoutePoint::getId, Function.identity()));

        List<RoutePoint> routePoints = new ArrayList<>();
        for (SaveRoutePointDto routePointDto : routePointsDto) {
            RoutePoint routePoint = routePointsById.getOrDefault(routePointDto.getId(), new RoutePoint());
            RetailPoint retailPoint = resolveRetailPoint(routePointDto.getRetailPointId());
            Set<Order> orders = resolveOrders(routePointDto.getOrderIds());
            routePointMapper.update(routePoint, routePointDto, route, retailPoint, orders);
            routePoints.add(routePoint);
        }
        return routePoints;
    }

    private RetailPoint resolveRetailPoint(Long retailPointId) {
        return repositoryGuard.execute(() -> retailPointRepository
                .findById(retailPointId)
                .orElseThrow(() -> new NotFoundException(RetailPoint.class, retailPointId)));
    }

    private Set<Order> resolveOrders(List<Long> orderIds) {
        if (orderIds.isEmpty()) {
            return Set.of();
        }
        return new HashSet<>(repositoryGuard.execute(() -> orderRepository.findAllById(orderIds)));
    }
}
