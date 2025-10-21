package com.rendaxx.labs.service;

import com.rendaxx.labs.domain.Order;
import com.rendaxx.labs.domain.Route;
import com.rendaxx.labs.domain.RoutePoint;
import com.rendaxx.labs.dtos.SaveRouteDto;
import com.rendaxx.labs.exceptions.NotFoundException;
import com.rendaxx.labs.mappers.RouteMapper;
import com.rendaxx.labs.repository.RouteRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class RouteService {

    RouteMapper mapper;
    RouteRepository repository;

    public Route create(SaveRouteDto command) {
        return save(command, new Route());
    }

    @Transactional(readOnly = true)
    public Route getById(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException(Route.class, id));
    }

    @Transactional(readOnly = true)
    public List<Route> getAll() {
        return repository.findAll();
    }

    public Route update(Long id, SaveRouteDto command) {
        Route route = repository.findById(id).orElseThrow(() -> new NotFoundException(Route.class, id));
        return save(command, route);
    }

    public void delete(Long id) {
        Route route = repository.findById(id).orElseThrow(() -> new NotFoundException(Route.class, id));
        repository.delete(route);
    }

    public void recalculateRoutePointOrderNumber(Route route) {
        for (int i = 0; i < route.getRoutePoints().size(); i++) {
            route.getRoutePoints().get(i).setOrderNumber(i);
        }
    }

    private Route save(SaveRouteDto command, Route route) {
        mapper.update(route, command);
        return repository.save(route);
    }
}
