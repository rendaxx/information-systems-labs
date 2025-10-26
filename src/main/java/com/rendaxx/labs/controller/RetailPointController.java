package com.rendaxx.labs.controller;

import com.rendaxx.labs.dtos.RetailPointDto;
import com.rendaxx.labs.dtos.SaveRetailPointDto;
import com.rendaxx.labs.service.RetailPointService;
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
@RequestMapping("/api/retail-points")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Validated
public class RetailPointController {

    RetailPointService retailPointService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RetailPointDto create(@RequestBody @Valid SaveRetailPointDto dto) {
        return retailPointService.create(dto);
    }

    @GetMapping("/{id}")
    public RetailPointDto getById(@PathVariable Long id) {
        return retailPointService.getById(id);
    }

    @GetMapping
    public List<RetailPointDto> getAll() {
        return retailPointService.getAll();
    }

    @PutMapping("/{id}")
    public RetailPointDto update(@PathVariable Long id, @RequestBody @Valid SaveRetailPointDto dto) {
        return retailPointService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        retailPointService.delete(id);
    }
}
