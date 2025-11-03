package com.rendaxx.labs.controller;

import com.rendaxx.labs.dtos.SaveVehicleDto;
import com.rendaxx.labs.dtos.VehicleDto;
import com.rendaxx.labs.service.VehicleService;
import jakarta.validation.Valid;
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
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Validated
public class VehicleController {

    VehicleService vehicleService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VehicleDto create(@RequestBody @Valid SaveVehicleDto dto) {
        return vehicleService.create(dto);
    }

    @GetMapping("/{id}")
    public VehicleDto getById(@PathVariable Long id) {
        return vehicleService.getById(id);
    }

    @GetMapping
    public Page<VehicleDto> getAll(Pageable pageable, @RequestParam Map<String, String> filters) {
        return vehicleService.getAll(pageable, filters);
    }

    @PutMapping("/{id}")
    public VehicleDto update(@PathVariable Long id, @RequestBody @Valid SaveVehicleDto dto) {
        return vehicleService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        vehicleService.delete(id);
    }
}
