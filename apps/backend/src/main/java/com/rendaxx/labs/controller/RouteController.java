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
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
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
    public ResponseEntity<RouteApiDto> getRoute(@PathVariable("id") Long id) {
        return ResponseEntity.ok(routeApiMapper.toApi(routeService.getById(id)));
    }

    @Override
    public ResponseEntity<PageRouteApiDto> listRoutes(
            @Nullable Integer page,
            @Nullable Integer size,
            @Nullable List<String> sort,
            @Nullable Map<String, String> filter) {
        Pageable pageable = pageRequestFactory.build(page, size, sort);
        Map<String, String> filters = filterParameterMapper.toFilters(filter != null ? filter : Map.of());
        Page<RouteDto> result = routeService.getAll(pageable, filters);
        PageRouteApiDto response = routeApiMapper.toRoutePage(result);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<RouteApiDto> updateRoute(
            @PathVariable("id") Long id, @Valid SaveRouteApiDto saveRouteApiDto) {
        SaveRouteDto command = routeApiMapper.toDto(saveRouteApiDto);
        RouteDto updated = routeService.update(id, command);
        return ResponseEntity.ok(routeApiMapper.toApi(updated));
    }

    @Override
    public ResponseEntity<Void> deleteRoute(@PathVariable("id") Long id) {
        routeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Double> getAverageRouteMileage() {
        BigDecimal average = routeService.getAverageMileageInKm();
        return ResponseEntity.ok(average.doubleValue());
    }

    @Override
    public ResponseEntity<List<RouteApiDto>> getRoutesWithinPeriod(
            @RequestParam("periodStart") LocalDateTime periodStart,
            @RequestParam("periodEnd") LocalDateTime periodEnd) {
        List<RouteDto> routes = routeService.getWithinPeriod(periodStart, periodEnd);
        return ResponseEntity.ok(routeApiMapper.toApi(routes));
    }

    @Override
    public ResponseEntity<List<RouteApiDto>> getRoutesByRetailPoint(@PathVariable("retailPointId") Long retailPointId) {
        List<RouteDto> routes = routeService.getByRetailPointId(retailPointId);
        return ResponseEntity.ok(routeApiMapper.toApi(routes));
    }
}
