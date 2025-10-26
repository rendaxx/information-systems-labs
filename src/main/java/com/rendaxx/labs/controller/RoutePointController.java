package com.rendaxx.labs.controller;

import com.rendaxx.labs.dtos.RetailPointDto;
import com.rendaxx.labs.dtos.RoutePointDto;
import com.rendaxx.labs.dtos.SaveRoutePointDto;
import com.rendaxx.labs.service.RoutePointService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/route-points")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Validated
public class RoutePointController {

    RoutePointService routePointService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoutePointDto create(@RequestBody @Valid SaveRoutePointDto dto) {
        return routePointService.create(dto);
    }

    @GetMapping("/{id}")
    public RoutePointDto getById(@PathVariable Long id) {
        return routePointService.getById(id);
    }

    @GetMapping
    public Page<RoutePointDto> getAll(Pageable pageable, @RequestParam Map<String, String> filters) {
        return routePointService.getAll(pageable, filters);
    }

    @PutMapping("/{id}")
    public RoutePointDto update(@PathVariable Long id, @RequestBody @Valid SaveRoutePointDto dto) {
        return routePointService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        routePointService.delete(id);
    }

    @GetMapping("/top-retail-points")
    public List<RetailPointDto> getTopRetailPoints(@RequestParam("limit") int limit) {
        return routePointService.getTopRetailPoints(limit);
    }
}
