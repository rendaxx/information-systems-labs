package com.rendaxx.labs.controller;

import com.rendaxx.labs.api.v1.api.RoutePointsApi;
import com.rendaxx.labs.api.v1.model.PageRoutePointApiDto;
import com.rendaxx.labs.api.v1.model.RetailPointApiDto;
import com.rendaxx.labs.api.v1.model.RoutePointApiDto;
import com.rendaxx.labs.api.v1.model.SaveRoutePointApiDto;
import com.rendaxx.labs.controller.support.FilterParameterMapper;
import com.rendaxx.labs.controller.support.PageRequestFactory;
import com.rendaxx.labs.dtos.RetailPointDto;
import com.rendaxx.labs.dtos.RoutePointDto;
import com.rendaxx.labs.dtos.SaveRoutePointDto;
import com.rendaxx.labs.mappers.api.RetailPointApiMapper;
import com.rendaxx.labs.mappers.api.RoutePointApiMapper;
import com.rendaxx.labs.service.RoutePointService;
import jakarta.validation.Valid;
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
public class RoutePointController implements RoutePointsApi {

    RoutePointService routePointService;
    RoutePointApiMapper routePointApiMapper;
    RetailPointApiMapper retailPointApiMapper;
    PageRequestFactory pageRequestFactory;
    FilterParameterMapper filterParameterMapper;

    @Override
    public ResponseEntity<RoutePointApiDto> createRoutePoint(@Valid SaveRoutePointApiDto saveRoutePointApiDto) {
        SaveRoutePointDto command = routePointApiMapper.toDto(saveRoutePointApiDto);
        RoutePointDto created = routePointService.create(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(routePointApiMapper.toApi(created));
    }

    @Override
    public ResponseEntity<RoutePointApiDto> getRoutePoint(Long id) {
        return ResponseEntity.ok(routePointApiMapper.toApi(routePointService.getById(id)));
    }

    @Override
    public ResponseEntity<PageRoutePointApiDto> listRoutePoints(
            Integer page,
            Integer size,
            List<String> sort,
            Map<String, String> filter
    ) {
        Pageable pageable = pageRequestFactory.build(page, size, sort);
        Page<RoutePointDto> result = routePointService.getAll(pageable, filterParameterMapper.toFilters(filter));
        return ResponseEntity.ok(routePointApiMapper.toRoutePointPage(result));
    }

    @Override
    public ResponseEntity<RoutePointApiDto> updateRoutePoint(Long id, @Valid SaveRoutePointApiDto saveRoutePointApiDto) {
        SaveRoutePointDto command = routePointApiMapper.toDto(saveRoutePointApiDto);
        RoutePointDto updated = routePointService.update(id, command);
        return ResponseEntity.ok(routePointApiMapper.toApi(updated));
    }

    @Override
    public ResponseEntity<Void> deleteRoutePoint(Long id) {
        routePointService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<List<RetailPointApiDto>> getTopRetailPoints(Integer limit) {
        List<RetailPointDto> topRetailPoints = routePointService.getTopRetailPoints(limit);
        return ResponseEntity.ok(retailPointApiMapper.toApi(topRetailPoints));
    }
}
