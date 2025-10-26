package com.rendaxx.labs.service;

import com.rendaxx.labs.domain.Route;
import com.rendaxx.labs.dtos.RouteDto;
import com.rendaxx.labs.dtos.SaveRouteDto;
import com.rendaxx.labs.events.EntityChangePublisher;
import com.rendaxx.labs.events.EntityChangeType;
import com.rendaxx.labs.exceptions.NotFoundException;
import com.rendaxx.labs.mappers.RouteMapper;
import com.rendaxx.labs.repository.RouteRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class RouteService {

    private static final int MILEAGE_SCALE = 3;

    RouteMapper mapper;
    RouteRepository repository;
    EntityChangePublisher changePublisher;

    private static final String DESTINATION = "/topic/routes";

    public RouteDto create(SaveRouteDto command) {
        Route route = save(command, new Route());
        RouteDto dto = mapper.toDto(route);
        changePublisher.publish(DESTINATION, route.getId(), dto, EntityChangeType.CREATED);
        return dto;
    }

    @Transactional(readOnly = true)
    public RouteDto getById(Long id) {
        Route route = repository.findById(id).orElseThrow(() -> new NotFoundException(Route.class, id));
        return mapper.toDto(route);
    }

    @Transactional(readOnly = true)
    public List<RouteDto> getAll() {
        return mapper.toDto(repository.findAll());
    }

    public RouteDto update(Long id, SaveRouteDto command) {
        Route route = repository.findById(id).orElseThrow(() -> new NotFoundException(Route.class, id));
        Route savedRoute = save(command, route);
        RouteDto dto = mapper.toDto(savedRoute);
        changePublisher.publish(DESTINATION, savedRoute.getId(), dto, EntityChangeType.UPDATED);
        return dto;
    }

    public void delete(Long id) {
        Route route = repository.findById(id).orElseThrow(() -> new NotFoundException(Route.class, id));
        repository.delete(route);
        changePublisher.publish(DESTINATION, route.getId(), null, EntityChangeType.DELETED);
    }

    public void recalculateRoutePointOrderNumber(Route route) {
        for (int i = 0; i < route.getRoutePoints().size(); i++) {
            route.getRoutePoints().get(i).setOrderNumber(i);
        }
    }

    @Transactional(readOnly = true)
    public BigDecimal getAverageMileageInKm() {
        BigDecimal averageMileage = repository.findAverageMileageInKm();
        return Objects
                .requireNonNullElse(averageMileage, BigDecimal.ZERO)
                .setScale(MILEAGE_SCALE, RoundingMode.HALF_UP);
    }

    @Transactional(readOnly = true)
    public List<RouteDto> getWithinPeriod(LocalDateTime periodStart, LocalDateTime periodEnd) {
        if (periodStart == null || periodEnd == null) {
            throw new IllegalArgumentException("Period bounds must be provided");
        }
        if (periodStart.isAfter(periodEnd)) {
            throw new IllegalArgumentException("Period start must not be after period end");
        }
        return mapper.toDto(repository.findWithinPeriodWithDetails(periodStart, periodEnd));
    }

    @Transactional(readOnly = true)
    public List<RouteDto> getByRetailPointId(Long retailPointId) {
        if (retailPointId == null) {
            throw new IllegalArgumentException("Retail point id must be provided");
        }
        return mapper.toDto(repository.findAllByRetailPointId(retailPointId));
    }

    private Route save(SaveRouteDto command, Route route) {
        mapper.update(route, command);
        return repository.save(route);
    }
}
