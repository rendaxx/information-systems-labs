package com.rendaxx.labs.service;

import com.rendaxx.labs.domain.RetailPoint;
import com.rendaxx.labs.dtos.RetailPointDto;
import com.rendaxx.labs.dtos.SaveRetailPointDto;
import com.rendaxx.labs.events.EntityChangePublisher;
import com.rendaxx.labs.events.EntityChangeType;
import com.rendaxx.labs.exceptions.BadRequestException;
import com.rendaxx.labs.exceptions.NotFoundException;
import com.rendaxx.labs.mappers.RetailPointMapper;
import com.rendaxx.labs.repository.RetailPointRepository;
import com.rendaxx.labs.repository.support.RepositoryGuard;
import com.rendaxx.labs.service.specification.EqualitySpecificationBuilder;
import java.util.Objects;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class RetailPointService {

    RetailPointMapper mapper;
    RetailPointRepository repository;
    EntityChangePublisher changePublisher;
    EqualitySpecificationBuilder specificationBuilder;
    RepositoryGuard repositoryGuard;

    int maxNearestRetailPointLimit;

    private static final String DESTINATION = "/topic/retail-points";

    public RetailPointService(
            RetailPointMapper mapper,
            RetailPointRepository repository,
            EntityChangePublisher changePublisher,
            EqualitySpecificationBuilder specificationBuilder,
            RepositoryGuard repositoryGuard,
            @Value("${labs.retail-points.max-nearest-limit:1000}") int maxNearestRetailPointLimit) {
        this.mapper = mapper;
        this.repository = repository;
        this.changePublisher = changePublisher;
        this.specificationBuilder = specificationBuilder;
        this.repositoryGuard = repositoryGuard;
        this.maxNearestRetailPointLimit = maxNearestRetailPointLimit;
    }

    public RetailPointDto create(SaveRetailPointDto command) {
        RetailPoint retailPoint = save(command, new RetailPoint());
        RetailPointDto dto = mapper.toDto(retailPoint);
        changePublisher.publish(DESTINATION, Objects.requireNonNull(retailPoint.getId()), dto, EntityChangeType.CREATED);
        return dto;
    }

    @Transactional(readOnly = true)
    public RetailPointDto getById(Long id) {
        RetailPoint retailPoint = repositoryGuard.execute(
                () -> repository.findById(id).orElseThrow(() -> new NotFoundException(RetailPoint.class, id)));
        return mapper.toDto(retailPoint);
    }

    @Transactional(readOnly = true)
    public Page<RetailPointDto> getAll(Pageable pageable, Map<String, String> filters) {
        Specification<RetailPoint> specification = specificationBuilder.build(filters);
        Page<RetailPoint> result = repositoryGuard.execute(() -> repository.findAll(specification, pageable));
        return result.map(mapper::toDto);
    }

    @Transactional(readOnly = true, propagation = Propagation.REQUIRED)
    public List<RetailPointDto> getNearestRetailPoints(Long retailPointId, int limit) {
        if (limit <= 0) {
            throw new BadRequestException("Limit must be positive");
        }
        int effectiveLimit = Math.min(limit, maxNearestRetailPointLimit);
        RetailPoint origin = repositoryGuard.execute(() -> repository
                .findById(retailPointId)
                .orElseThrow(() -> new NotFoundException(RetailPoint.class, retailPointId)));

        Long originId = Objects.requireNonNull(origin.getId());
        List<RetailPoint> nearest =
                repositoryGuard.execute(() -> repository.findNearestRetailPoints(originId, effectiveLimit));
        return mapper.toDto(nearest);
    }

    public RetailPointDto update(Long id, SaveRetailPointDto command) {
        RetailPoint retailPoint = repositoryGuard.execute(
                () -> repository.findById(id).orElseThrow(() -> new NotFoundException(RetailPoint.class, id)));
        RetailPoint savedRetailPoint = save(command, retailPoint);
        RetailPointDto dto = mapper.toDto(savedRetailPoint);
        changePublisher.publish(
                DESTINATION, Objects.requireNonNull(savedRetailPoint.getId()), dto, EntityChangeType.UPDATED);
        return dto;
    }

    public void delete(Long id) {
        RetailPoint retailPoint = repositoryGuard.execute(
                () -> repository.findById(id).orElseThrow(() -> new NotFoundException(RetailPoint.class, id)));
        repositoryGuard.execute(() -> repository.delete(retailPoint));
        changePublisher.publish(DESTINATION, Objects.requireNonNull(retailPoint.getId()), null, EntityChangeType.DELETED);
    }

    private RetailPoint save(SaveRetailPointDto command, RetailPoint retailPoint) {
        mapper.update(retailPoint, command);
        return repositoryGuard.execute(() -> repository.save(retailPoint));
    }
}
