package com.rendaxx.labs.service;

import com.rendaxx.labs.domain.RetailPoint;
import com.rendaxx.labs.dtos.RetailPointDto;
import com.rendaxx.labs.dtos.SaveRetailPointDto;
import com.rendaxx.labs.events.EntityChangePublisher;
import com.rendaxx.labs.events.EntityChangeType;
import com.rendaxx.labs.exceptions.NotFoundException;
import com.rendaxx.labs.mappers.RetailPointMapper;
import com.rendaxx.labs.repository.RetailPointRepository;
import com.rendaxx.labs.service.specification.EqualitySpecificationBuilder;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class RetailPointService {

    RetailPointMapper mapper;
    RetailPointRepository repository;
    EntityChangePublisher changePublisher;
    EqualitySpecificationBuilder specificationBuilder;

    private static final String DESTINATION = "/topic/retail-points";

    public RetailPointDto create(SaveRetailPointDto command) {
        RetailPoint retailPoint = save(command, new RetailPoint());
        RetailPointDto dto = mapper.toDto(retailPoint);
        changePublisher.publish(DESTINATION, retailPoint.getId(), dto, EntityChangeType.CREATED);
        return dto;
    }

    @Transactional(readOnly = true)
    public RetailPointDto getById(Long id) {
        RetailPoint retailPoint =
                repository.findById(id).orElseThrow(() -> new NotFoundException(RetailPoint.class, id));
        return mapper.toDto(retailPoint);
    }

    @Transactional(readOnly = true)
    public Page<RetailPointDto> getAll(Pageable pageable, Map<String, String> filters) {
        Specification<RetailPoint> specification = specificationBuilder.build(filters);
        return repository.findAll(specification, pageable).map(mapper::toDto);
    }

    @Transactional(readOnly = true, propagation = Propagation.REQUIRED)
    public List<RetailPointDto> getNearestRetailPoints(Long retailPointId, int limit) {
        if (retailPointId == null) {
            throw new IllegalArgumentException("Retail point id must be provided");
        }
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit must be positive");
        }
        RetailPoint origin = repository
                .findById(retailPointId)
                .orElseThrow(() -> new NotFoundException(RetailPoint.class, retailPointId));

        List<RetailPoint> nearest = repository.findNearestRetailPoints(origin.getId(), limit);
        if (nearest.isEmpty()) {
            return Collections.emptyList();
        }
        return mapper.toDto(nearest);
    }

    public RetailPointDto update(Long id, SaveRetailPointDto command) {
        RetailPoint retailPoint =
                repository.findById(id).orElseThrow(() -> new NotFoundException(RetailPoint.class, id));
        RetailPoint savedRetailPoint = save(command, retailPoint);
        RetailPointDto dto = mapper.toDto(savedRetailPoint);
        changePublisher.publish(DESTINATION, savedRetailPoint.getId(), dto, EntityChangeType.UPDATED);
        return dto;
    }

    public void delete(Long id) {
        RetailPoint retailPoint =
                repository.findById(id).orElseThrow(() -> new NotFoundException(RetailPoint.class, id));
        repository.delete(retailPoint);
        changePublisher.publish(DESTINATION, retailPoint.getId(), null, EntityChangeType.DELETED);
    }

    private RetailPoint save(SaveRetailPointDto command, RetailPoint retailPoint) {
        mapper.update(retailPoint, command);
        return repository.save(retailPoint);
    }
}
