package com.rendaxx.labs.service;

import com.rendaxx.labs.domain.RetailPoint;
import com.rendaxx.labs.domain.RoutePoint;
import com.rendaxx.labs.dtos.RetailPointDto;
import com.rendaxx.labs.dtos.RoutePointDto;
import com.rendaxx.labs.dtos.SaveRoutePointDto;
import com.rendaxx.labs.events.EntityChangePublisher;
import com.rendaxx.labs.events.EntityChangeType;
import com.rendaxx.labs.exceptions.NotFoundException;
import com.rendaxx.labs.mappers.RetailPointMapper;
import com.rendaxx.labs.mappers.RoutePointMapper;
import com.rendaxx.labs.repository.RoutePointRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import org.springframework.data.domain.PageRequest;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class RoutePointService {

    RoutePointMapper mapper;
    RoutePointRepository repository;
    RetailPointMapper retailPointMapper;

    RouteService routeService;
    EntityChangePublisher changePublisher;

    private static final String DESTINATION = "/topic/route-points";

    public RoutePointDto create(SaveRoutePointDto command) {
        RoutePoint routePoint = save(command, new RoutePoint());
        RoutePointDto dto = mapper.toDto(routePoint);
        changePublisher.publish(DESTINATION, routePoint.getId(), dto, EntityChangeType.CREATED);
        return dto;
    }

    @Transactional(readOnly = true)
    public RoutePointDto getById(Long id) {
        RoutePoint routePoint = repository.findById(id).orElseThrow(() -> new NotFoundException(RoutePoint.class, id));
        return mapper.toDto(routePoint);
    }

    @Transactional(readOnly = true)
    public List<RoutePointDto> getAll() {
        return mapper.toDto(repository.findAll());
    }

    public RoutePointDto update(Long id, SaveRoutePointDto command) {
        RoutePoint routePoint = repository.findById(id).orElseThrow(() -> new NotFoundException(RoutePoint.class, id));
        RoutePoint savedRoutePoint = save(command, routePoint);
        RoutePointDto dto = mapper.toDto(savedRoutePoint);
        changePublisher.publish(DESTINATION, savedRoutePoint.getId(), dto, EntityChangeType.UPDATED);
        return dto;
    }

    public void delete(Long id) {
        RoutePoint routePoint = repository.findById(id).orElseThrow(() -> new NotFoundException(RoutePoint.class, id));
        Long routePointId = routePoint.getId();
        routePoint.getRoute().getRoutePoints().removeIf(rp -> rp.getId().equals(routePointId));
        routeService.recalculateRoutePointOrderNumber(routePoint.getRoute());
        repository.delete(routePoint);
        changePublisher.publish(DESTINATION, routePointId, null, EntityChangeType.DELETED);
    }

    @Transactional(readOnly = true)
    public List<RetailPointDto> getTopRetailPoints(int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit must be positive");
        }
        List<RetailPoint> retailPoints = repository.findMostVisitedRetailPoints(PageRequest.of(0, limit));
        if (retailPoints.isEmpty()) {
            return Collections.emptyList();
        }
        return retailPointMapper.toDto(retailPoints);
    }

    private RoutePoint save(SaveRoutePointDto command, RoutePoint routePoint) {
        mapper.update(routePoint, command);
        return repository.save(routePoint);
    }
}
