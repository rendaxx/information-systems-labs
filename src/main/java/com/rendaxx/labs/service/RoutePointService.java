package com.rendaxx.labs.service;

import com.rendaxx.labs.domain.Order;
import com.rendaxx.labs.domain.RoutePoint;
import com.rendaxx.labs.dtos.SaveRoutePointDto;
import com.rendaxx.labs.exceptions.NotFoundException;
import com.rendaxx.labs.mappers.RoutePointMapper;
import com.rendaxx.labs.repository.RoutePointRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class RoutePointService {

    RoutePointMapper mapper;
    RoutePointRepository repository;

    RouteService routeService;

    public RoutePoint create(SaveRoutePointDto command) {
        return save(command, new RoutePoint());
    }

    @Transactional(readOnly = true)
    public RoutePoint getById(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException(RoutePoint.class, id));
    }

    @Transactional(readOnly = true)
    public List<RoutePoint> getAll() {
        return repository.findAll();
    }

    public RoutePoint update(Long id, SaveRoutePointDto command) {
        RoutePoint routePoint = repository.findById(id).orElseThrow(() -> new NotFoundException(RoutePoint.class, id));
        return save(command, routePoint);
    }

    public void delete(Long id) {
        RoutePoint routePoint = repository.findById(id).orElseThrow(() -> new NotFoundException(RoutePoint.class, id));
        routePoint.getRoute().getRoutePoints().removeIf(rp -> rp.getId().equals(routePoint.getId()));
        routeService.recalculateRoutePointOrderNumber(routePoint.getRoute());
        repository.delete(routePoint);
    }

    private RoutePoint save(SaveRoutePointDto command, RoutePoint routePoint) {
        mapper.update(routePoint, command);
        return repository.save(routePoint);
    }
}
