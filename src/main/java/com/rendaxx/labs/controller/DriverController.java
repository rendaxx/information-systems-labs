package com.rendaxx.labs.controller;

import com.rendaxx.labs.dtos.DriverDto;
import com.rendaxx.labs.dtos.SaveDriverDto;
import com.rendaxx.labs.mappers.DriverMapper;
import com.rendaxx.labs.service.DriverService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Validated
public class DriverController {

    DriverService driverService;
    DriverMapper driverMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DriverDto create(@RequestBody @Valid SaveDriverDto dto) {
        return driverMapper.toDto(driverService.create(dto));
    }

    @GetMapping("/{id}")
    public DriverDto getById(@PathVariable Long id) {
        return driverMapper.toDto(driverService.getById(id));
    }

    @GetMapping
    public List<DriverDto> getAll() {
        return driverMapper.toDto(driverService.getAll());
    }

    @PutMapping("/{id}")
    public DriverDto update(@PathVariable Long id, @RequestBody @Valid SaveDriverDto dto) {
        return driverMapper.toDto(driverService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        driverService.delete(id);
    }
}
