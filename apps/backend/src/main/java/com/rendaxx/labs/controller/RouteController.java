package com.rendaxx.labs.controller;

import com.rendaxx.labs.api.v1.api.RoutesApi;
import com.rendaxx.labs.api.v1.model.PageRouteApiDto;
import com.rendaxx.labs.api.v1.model.RouteApiDto;
import com.rendaxx.labs.api.v1.model.SaveRouteApiDto;
import com.rendaxx.labs.controller.support.FilterParameterMapper;
import com.rendaxx.labs.controller.support.PageRequestFactory;
import com.rendaxx.labs.dtos.RouteDto;
import com.rendaxx.labs.dtos.SaveRouteDto;
import com.rendaxx.labs.mappers.api.RouteApiMapper;
import com.rendaxx.labs.service.RouteService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Validated
public class RouteController implements RoutesApi {

    RouteService routeService;
    RouteApiMapper routeApiMapper;
    PageRequestFactory pageRequestFactory;
    FilterParameterMapper filterParameterMapper;

    @Override
    public ResponseEntity<RouteApiDto> createRoute(@Valid SaveRouteApiDto saveRouteApiDto) {
        SaveRouteDto command = routeApiMapper.toDto(saveRouteApiDto);
        RouteDto created = routeService.create(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(routeApiMapper.toApi(created));
    }

    @Override
    public ResponseEntity<RouteApiDto> getRoute(Long id) {
        return ResponseEntity.ok(routeApiMapper.toApi(routeService.getById(id)));
    }

    @Override
    public ResponseEntity<PageRouteApiDto> listRoutes(
            Integer page, Integer size, List<String> sort, Map<String, String> filter) {
        Pageable pageable = pageRequestFactory.build(page, size, sort);
        Page<RouteDto> result = routeService.getAll(pageable, filterParameterMapper.toFilters(filter));
        return ResponseEntity.ok(routeApiMapper.toRoutePage(result));
    }

    @Override
    public ResponseEntity<RouteApiDto> updateRoute(Long id, @Valid SaveRouteApiDto saveRouteApiDto) {
        SaveRouteDto command = routeApiMapper.toDto(saveRouteApiDto);
        RouteDto updated = routeService.update(id, command);
        return ResponseEntity.ok(routeApiMapper.toApi(updated));
    }

    @Override
    public ResponseEntity<Void> deleteRoute(Long id) {
        routeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Double> getAverageRouteMileage() {
        BigDecimal average = routeService.getAverageMileageInKm();
        return ResponseEntity.ok(average == null ? null : average.doubleValue());
    }

    @Override
    public ResponseEntity<List<RouteApiDto>> getRoutesWithinPeriod(LocalDateTime periodStart, LocalDateTime periodEnd) {
        List<RouteDto> routes = routeService.getWithinPeriod(periodStart, periodEnd);
        return ResponseEntity.ok(routeApiMapper.toApi(routes));
    }

    @Override
    public ResponseEntity<List<RouteApiDto>> getRoutesByRetailPoint(Long retailPointId) {
        List<RouteDto> routes = routeService.getByRetailPointId(retailPointId);
        return ResponseEntity.ok(routeApiMapper.toApi(routes));
    }
}
