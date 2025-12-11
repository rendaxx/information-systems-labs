package com.rendaxx.labs.controller;

import com.rendaxx.labs.api.v1.api.RetailPointsApi;
import com.rendaxx.labs.api.v1.model.PageRetailPointApiDto;
import com.rendaxx.labs.api.v1.model.RetailPointApiDto;
import com.rendaxx.labs.api.v1.model.SaveRetailPointApiDto;
import com.rendaxx.labs.controller.support.FilterParameterMapper;
import com.rendaxx.labs.controller.support.PageRequestFactory;
import com.rendaxx.labs.dtos.RetailPointDto;
import com.rendaxx.labs.dtos.SaveRetailPointDto;
import com.rendaxx.labs.mappers.api.RetailPointApiMapper;
import com.rendaxx.labs.service.RetailPointService;
import jakarta.validation.Valid;
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
public class RetailPointController implements RetailPointsApi {

    RetailPointService retailPointService;
    RetailPointApiMapper retailPointApiMapper;
    PageRequestFactory pageRequestFactory;
    FilterParameterMapper filterParameterMapper;

    @Override
    public ResponseEntity<RetailPointApiDto> createRetailPoint(@Valid SaveRetailPointApiDto saveRetailPointApiDto) {
        SaveRetailPointDto command = retailPointApiMapper.toDto(saveRetailPointApiDto);
        RetailPointDto created = retailPointService.create(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(retailPointApiMapper.toApi(created));
    }

    @Override
    public ResponseEntity<RetailPointApiDto> getRetailPoint(@PathVariable("id") Long id) {
        return ResponseEntity.ok(retailPointApiMapper.toApi(retailPointService.getById(id)));
    }

    @Override
    public ResponseEntity<PageRetailPointApiDto> listRetailPoints(
            @Nullable Integer page,
            @Nullable Integer size,
            @Nullable List<String> sort,
            @Nullable Map<String, String> filter) {
        Pageable pageable = pageRequestFactory.build(page, size, sort);
        Map<String, String> filters = filterParameterMapper.toFilters(filter != null ? filter : Map.of());
        Page<RetailPointDto> result = retailPointService.getAll(pageable, filters);
        PageRetailPointApiDto response = retailPointApiMapper.toRetailPointPage(result);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<RetailPointApiDto> updateRetailPoint(
            @PathVariable("id") Long id, @Valid SaveRetailPointApiDto saveRetailPointApiDto) {
        SaveRetailPointDto command = retailPointApiMapper.toDto(saveRetailPointApiDto);
        RetailPointDto updated = retailPointService.update(id, command);
        return ResponseEntity.ok(retailPointApiMapper.toApi(updated));
    }

    @Override
    public ResponseEntity<Void> deleteRetailPoint(@PathVariable("id") Long id) {
        retailPointService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<List<RetailPointApiDto>> getNearestRetailPoints(
            @PathVariable("id") Long id, @RequestParam("limit") Integer limit) {
        List<RetailPointDto> nearest = retailPointService.getNearestRetailPoints(id, limit);
        return ResponseEntity.ok(retailPointApiMapper.toApi(nearest));
    }
}
