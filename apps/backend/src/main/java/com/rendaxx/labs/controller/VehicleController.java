package com.rendaxx.labs.controller;

import com.rendaxx.labs.api.v1.api.VehiclesApi;
import com.rendaxx.labs.api.v1.model.PageVehicleApiDto;
import com.rendaxx.labs.api.v1.model.SaveVehicleApiDto;
import com.rendaxx.labs.api.v1.model.VehicleApiDto;
import com.rendaxx.labs.controller.support.FilterParameterMapper;
import com.rendaxx.labs.controller.support.PageRequestFactory;
import com.rendaxx.labs.dtos.SaveVehicleDto;
import com.rendaxx.labs.dtos.VehicleDto;
import com.rendaxx.labs.mappers.api.VehicleApiMapper;
import com.rendaxx.labs.service.VehicleService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Validated
public class VehicleController implements VehiclesApi {

    VehicleService vehicleService;
    VehicleApiMapper vehicleApiMapper;
    PageRequestFactory pageRequestFactory;
    FilterParameterMapper filterParameterMapper;

    @Override
    public ResponseEntity<VehicleApiDto> createVehicle(@Valid SaveVehicleApiDto saveVehicleApiDto) {
        SaveVehicleDto command = vehicleApiMapper.toDto(saveVehicleApiDto);
        VehicleDto created = vehicleService.create(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(vehicleApiMapper.toApi(created));
    }

    @Override
    public ResponseEntity<VehicleApiDto> getVehicle(@PathVariable("id") Long id) {
        return ResponseEntity.ok(vehicleApiMapper.toApi(vehicleService.getById(id)));
    }

    @Override
    public ResponseEntity<PageVehicleApiDto> listVehicles(
            Integer page, Integer size, List<String> sort, Map<String, String> filter) {
        Pageable pageable = pageRequestFactory.build(page, size, sort);
        Page<VehicleDto> result = vehicleService.getAll(pageable, filterParameterMapper.toFilters(filter));
        PageVehicleApiDto response = vehicleApiMapper.toVehiclePage(result);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<VehicleApiDto> updateVehicle(
            @PathVariable("id") Long id, @Valid SaveVehicleApiDto saveVehicleApiDto) {
        SaveVehicleDto command = vehicleApiMapper.toDto(saveVehicleApiDto);
        VehicleDto updated = vehicleService.update(id, command);
        return ResponseEntity.ok(vehicleApiMapper.toApi(updated));
    }

    @Override
    public ResponseEntity<Void> deleteVehicle(@PathVariable("id") Long id) {
        vehicleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
