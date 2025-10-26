package com.rendaxx.labs.controller;

import com.rendaxx.labs.dtos.RouteDto;
import com.rendaxx.labs.dtos.SaveRouteDto;
import com.rendaxx.labs.service.RouteService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.format.annotation.DateTimeFormat;
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
@RequestMapping("/api/routes")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Validated
public class RouteController {

    RouteService routeService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RouteDto create(@RequestBody @Valid SaveRouteDto dto) {
        return routeService.create(dto);
    }

    @GetMapping("/{id}")
    public RouteDto getById(@PathVariable Long id) {
        return routeService.getById(id);
    }

    @GetMapping
    public List<RouteDto> getAll() {
        return routeService.getAll();
    }

    @PutMapping("/{id}")
    public RouteDto update(@PathVariable Long id, @RequestBody @Valid SaveRouteDto dto) {
        return routeService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        routeService.delete(id);
    }

    @GetMapping("/average-mileage")
    public BigDecimal getAverageMileage() {
        return routeService.getAverageMileageInKm();
    }

    @GetMapping("/within-period")
    public List<RouteDto> getWithinPeriod(
        @RequestParam("periodStart")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime periodStart,
        @RequestParam("periodEnd")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime periodEnd
    ) {
        return routeService.getWithinPeriod(periodStart, periodEnd);
    }

    @GetMapping("/retail-point/{retailPointId}")
    public List<RouteDto> getByRetailPointId(@PathVariable Long retailPointId) {
        return routeService.getByRetailPointId(retailPointId);
    }
}
