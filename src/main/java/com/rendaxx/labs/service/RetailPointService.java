package com.rendaxx.labs.service;

import com.rendaxx.labs.domain.RetailPoint;
import com.rendaxx.labs.dtos.RetailPointDto;
import com.rendaxx.labs.dtos.SaveRetailPointDto;
import com.rendaxx.labs.exceptions.NotFoundException;
import com.rendaxx.labs.mappers.RetailPointMapper;
import com.rendaxx.labs.repository.RetailPointRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

import org.springframework.transaction.annotation.Propagation;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class RetailPointService {

    RetailPointMapper mapper;
    RetailPointRepository repository;

    public RetailPointDto create(SaveRetailPointDto command) {
        RetailPoint retailPoint = save(command, new RetailPoint());
        return mapper.toDto(retailPoint);
    }

    @Transactional(readOnly = true)
    public RetailPointDto getById(Long id) {
        RetailPoint retailPoint = repository.findById(id).orElseThrow(() -> new NotFoundException(RetailPoint.class, id));
        return mapper.toDto(retailPoint);
    }

    @Transactional(readOnly = true)
    public List<RetailPointDto> getAll() {
        return mapper.toDto(repository.findAll());
    }

    @Transactional(readOnly = true, propagation = Propagation.REQUIRED)
    public List<RetailPointDto> getNearestRetailPoints(Long retailPointId, int limit) {
        if (retailPointId == null) {
            throw new IllegalArgumentException("Retail point id must be provided");
        }
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit must be positive");
        }
        RetailPoint origin = repository.findById(retailPointId)
            .orElseThrow(() -> new NotFoundException(RetailPoint.class, retailPointId));

        List<RetailPoint> nearest = repository.findNearestRetailPoints(origin.getId(), limit);
        if (nearest.isEmpty()) {
            return Collections.emptyList();
        }
        return mapper.toDto(nearest);
    }

    public RetailPointDto update(Long id, SaveRetailPointDto command) {
        RetailPoint retailPoint = repository.findById(id).orElseThrow(() -> new NotFoundException(RetailPoint.class, id));
        RetailPoint savedRetailPoint = save(command, retailPoint);
        return mapper.toDto(savedRetailPoint);
    }

    public void delete(Long id) {
        RetailPoint retailPoint = repository.findById(id).orElseThrow(() -> new NotFoundException(RetailPoint.class, id));
        repository.delete(retailPoint);
    }

    private RetailPoint save(SaveRetailPointDto command, RetailPoint retailPoint) {
        mapper.update(retailPoint, command);
        return repository.save(retailPoint);
    }
}
