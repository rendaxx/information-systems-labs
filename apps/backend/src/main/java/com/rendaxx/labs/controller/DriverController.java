package com.rendaxx.labs.controller;

import com.rendaxx.labs.api.v1.api.DriversApi;
import com.rendaxx.labs.api.v1.model.DriverApiDto;
import com.rendaxx.labs.api.v1.model.PageDriverApiDto;
import com.rendaxx.labs.api.v1.model.SaveDriverApiDto;
import com.rendaxx.labs.controller.support.FilterParameterMapper;
import com.rendaxx.labs.controller.support.PageRequestFactory;
import com.rendaxx.labs.dtos.DriverDto;
import com.rendaxx.labs.dtos.SaveDriverDto;
import com.rendaxx.labs.mappers.api.DriverApiMapper;
import com.rendaxx.labs.mappers.api.PageResponseMapper;
import com.rendaxx.labs.service.DriverService;
import jakarta.validation.Valid;
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
public class DriverController implements DriversApi {

    DriverService driverService;
    DriverApiMapper driverApiMapper;
    PageResponseMapper pageResponseMapper;
    PageRequestFactory pageRequestFactory;
    FilterParameterMapper filterParameterMapper;

    @Override
    public ResponseEntity<DriverApiDto> createDriver(@Valid SaveDriverApiDto saveDriverApiDto) {
        SaveDriverDto command = driverApiMapper.toDto(saveDriverApiDto);
        DriverDto created = driverService.create(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(driverApiMapper.toApi(created));
    }

    @Override
    public ResponseEntity<DriverApiDto> getDriver(Long id) {
        return ResponseEntity.ok(driverApiMapper.toApi(driverService.getById(id)));
    }

    @Override
    public ResponseEntity<PageDriverApiDto> listDrivers(Integer page, Integer size, java.util.List<String> sort, Map<String, String> filter) {
        Pageable pageable = pageRequestFactory.build(page, size, sort);
        Page<DriverDto> result = driverService.getAll(pageable, filterParameterMapper.toFilters(filter));
        return ResponseEntity.ok(pageResponseMapper.toDriverPage(result));
    }

    @Override
    public ResponseEntity<DriverApiDto> updateDriver(Long id, @Valid SaveDriverApiDto saveDriverApiDto) {
        SaveDriverDto command = driverApiMapper.toDto(saveDriverApiDto);
        DriverDto updated = driverService.update(id, command);
        return ResponseEntity.ok(driverApiMapper.toApi(updated));
    }

    @Override
    public ResponseEntity<Void> deleteDriver(Long id) {
        driverService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
