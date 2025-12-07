package com.rendaxx.labs.service;

import com.rendaxx.labs.domain.Order;
import com.rendaxx.labs.domain.RetailPoint;
import com.rendaxx.labs.domain.Route;
import com.rendaxx.labs.domain.RoutePoint;
import com.rendaxx.labs.dtos.RetailPointDto;
import com.rendaxx.labs.dtos.RoutePointDto;
import com.rendaxx.labs.dtos.SaveRoutePointDto;
import com.rendaxx.labs.events.EntityChangePublisher;
import com.rendaxx.labs.events.EntityChangeType;
import com.rendaxx.labs.exceptions.BadRequestException;
import com.rendaxx.labs.exceptions.NotFoundException;
import com.rendaxx.labs.mappers.RetailPointMapper;
import com.rendaxx.labs.mappers.RoutePointMapper;
import com.rendaxx.labs.repository.OrderRepository;
import com.rendaxx.labs.repository.RetailPointRepository;
import com.rendaxx.labs.repository.RoutePointRepository;
import com.rendaxx.labs.repository.RouteRepository;
import com.rendaxx.labs.repository.support.RepositoryGuard;
import com.rendaxx.labs.repository.view.RetailPointView;
import com.rendaxx.labs.repository.view.RoutePointView;
import com.rendaxx.labs.service.specification.EqualitySpecificationBuilder;
import jakarta.annotation.Nullable;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class RoutePointService {

    RoutePointMapper mapper;
    RoutePointRepository repository;
    RetailPointMapper retailPointMapper;

    RouteService routeService;
    RouteRepository routeRepository;
    RetailPointRepository retailPointRepository;
    OrderRepository orderRepository;
    EntityChangePublisher changePublisher;
    EqualitySpecificationBuilder specificationBuilder;
    RepositoryGuard repositoryGuard;
    Clock clock;

    private static final String DESTINATION = "/topic/route-points";

    public RoutePointDto create(SaveRoutePointDto command) {
        ensureRouteAssociation(command, null);
        RoutePoint routePoint = save(command, new RoutePoint());
        RoutePointDto dto = repositoryGuard.execute(() -> repository
                .findViewById(Objects.requireNonNull(routePoint.getId()))
                .map(mapper::toDto)
                .orElseThrow(() -> new NotFoundException(RoutePoint.class, routePoint.getId())));
        changePublisher.publish(DESTINATION, Objects.requireNonNull(routePoint.getId()), dto, EntityChangeType.CREATED);
        return dto;
    }

    @Transactional(readOnly = true)
    public RoutePointDto getById(Long id) {
        RoutePointView routePoint = repositoryGuard.execute(
                () -> repository.findViewById(id).orElseThrow(() -> new NotFoundException(RoutePoint.class, id)));
        return mapper.toDto(routePoint);
    }

    @Transactional(readOnly = true)
    public Page<RoutePointDto> getAll(Pageable pageable, Map<String, String> filters) {
        Specification<RoutePoint> specification = specificationBuilder.build(filters);
        Page<RoutePointView> routePoints = repositoryGuard.execute(() ->
                repository.findBy(specification, q -> q.as(RoutePointView.class).page(pageable)));
        return routePoints.map(mapper::toDto);
    }

    public RoutePointDto update(Long id, SaveRoutePointDto command) {
        RoutePoint routePoint = repositoryGuard.execute(
                () -> repository.findById(id).orElseThrow(() -> new NotFoundException(RoutePoint.class, id)));
        ensureRouteAssociation(command, routePoint.getRoute());
        RoutePoint savedRoutePoint = save(command, routePoint);
        RoutePointDto dto = repositoryGuard.execute(() -> repository
                .findViewById(id)
                .map(mapper::toDto)
                .orElseThrow(() -> new NotFoundException(RoutePoint.class, id)));
        changePublisher.publish(
                DESTINATION, Objects.requireNonNull(savedRoutePoint.getId()), dto, EntityChangeType.UPDATED);
        return dto;
    }

    public void delete(Long id) {
        RoutePoint routePoint = repositoryGuard.execute(
                () -> repository.findById(id).orElseThrow(() -> new NotFoundException(RoutePoint.class, id)));
        Long routePointId = Objects.requireNonNull(routePoint.getId());
        routePoint.getRoute().getRoutePoints().removeIf(rp -> Objects.equals(rp.getId(), routePointId));
        routeService.recalculateRoutePointOrderNumber(routePoint.getRoute());
        repositoryGuard.execute(() -> repository.delete(routePoint));
        changePublisher.publish(DESTINATION, routePointId, null, EntityChangeType.DELETED);
    }

    @Transactional(readOnly = true)
    public List<RetailPointDto> getTopRetailPoints(int limit) {
        if (limit <= 0) {
            throw new BadRequestException("Limit must be positive");
        }
        List<RetailPointView> retailPoints =
                repositoryGuard.execute(() -> repository.findMostVisitedRetailPointsView(PageRequest.of(0, limit)));
        if (retailPoints.isEmpty()) {
            return Collections.emptyList();
        }
        return retailPointMapper.toDtoFromView(retailPoints);
    }

    private RoutePoint save(SaveRoutePointDto command, RoutePoint routePoint) {
        Route route = resolveRoute(command, routePoint.getRoute());
        RetailPoint retailPoint = repositoryGuard.execute(() -> retailPointRepository
                .findById(command.getRetailPointId())
                .orElseThrow(() -> new NotFoundException(RetailPoint.class, command.getRetailPointId())));
        Set<Order> orders =
                new HashSet<>(repositoryGuard.execute(() -> orderRepository.findAllById(command.getOrderIds())));
        mapper.update(routePoint, command, route, retailPoint, orders);
        return repositoryGuard.execute(() -> repository.save(routePoint));
    }

    private void ensureRouteAssociation(SaveRoutePointDto command, @Nullable Route existingRoute) {
        if (command.getRouteId() != null) {
            return;
        }
        if (existingRoute != null) {
            command.setRouteId(existingRoute.getId());
            return;
        }
        Route fallbackRoute = Route.builder()
                .plannedStartTime(defaultTime(command.getPlannedStartTime()))
                .plannedEndTime(defaultTime(command.getPlannedEndTime(), command.getPlannedStartTime()))
                .mileageInKm(BigDecimal.valueOf(1L))
                .build();
        Route persisted = repositoryGuard.execute(() -> routeRepository.save(fallbackRoute));
        command.setRouteId(persisted.getId());
    }

    private LocalDateTime defaultTime(LocalDateTime value) {
        return defaultTime(value, LocalDateTime.now(clock));
    }

    private LocalDateTime defaultTime(@Nullable LocalDateTime value, LocalDateTime fallback) {
        return value != null ? value : fallback;
    }

    private Route resolveRoute(SaveRoutePointDto dto, @Nullable Route parentRoute) {
        if (parentRoute != null) {
            return parentRoute;
        }

        Long routeId = dto.getRouteId();
        if (routeId == null) {
            throw new BadRequestException("Route id must be provided when parent route is not specified");
        }

        return repositoryGuard.execute(
                () -> routeRepository.findById(routeId).orElseThrow(() -> new NotFoundException(Route.class, routeId)));
    }
}
