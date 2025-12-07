package com.rendaxx.labs.service;

import com.rendaxx.labs.domain.Driver;
import com.rendaxx.labs.dtos.DriverDto;
import com.rendaxx.labs.dtos.SaveDriverDto;
import com.rendaxx.labs.events.EntityChangePublisher;
import com.rendaxx.labs.events.EntityChangeType;
import com.rendaxx.labs.exceptions.NotFoundException;
import com.rendaxx.labs.mappers.DriverMapper;
import com.rendaxx.labs.repository.DriverRepository;
import com.rendaxx.labs.repository.support.RepositoryGuard;
import com.rendaxx.labs.repository.view.DriverView;
import com.rendaxx.labs.service.specification.EqualitySpecificationBuilder;
import java.util.Map;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class DriverService {

    DriverMapper mapper;
    DriverRepository repository;
    EntityChangePublisher changePublisher;
    EqualitySpecificationBuilder specificationBuilder;
    RepositoryGuard repositoryGuard;

    private static final String DESTINATION = "/topic/drivers";

    public DriverDto create(SaveDriverDto command) {
        Driver driver = save(command, new Driver());
        DriverDto dto = repositoryGuard.execute(() -> repository
                .findViewById(Objects.requireNonNull(driver.getId()))
                .map(mapper::toDto)
                .orElseThrow(() -> new NotFoundException(Driver.class, driver.getId())));
        changePublisher.publish(DESTINATION, Objects.requireNonNull(driver.getId()), dto, EntityChangeType.CREATED);
        return dto;
    }

    @Transactional(readOnly = true)
    public DriverDto getById(Long id) {
        return repositoryGuard.execute(() -> repository
                .findViewById(id)
                .map(mapper::toDto)
                .orElseThrow(() -> new NotFoundException(Driver.class, id)));
    }

    @Transactional(readOnly = true)
    public Page<DriverDto> getAll(Pageable pageable, Map<String, String> filters) {
        Specification<Driver> specification = specificationBuilder.build(filters);
        Page<DriverView> drivers = repositoryGuard.execute(() ->
                repository.findBy(specification, q -> q.as(DriverView.class).page(pageable)));
        return drivers.map(mapper::toDto);
    }

    public DriverDto update(Long id, SaveDriverDto command) {
        Driver driver = repositoryGuard.execute(
                () -> repository.findById(id).orElseThrow(() -> new NotFoundException(Driver.class, id)));
        Driver savedDriver = save(command, driver);
        DriverDto dto = repositoryGuard.execute(() -> repository
                .findViewById(id)
                .map(mapper::toDto)
                .orElseThrow(() -> new NotFoundException(Driver.class, id)));
        changePublisher.publish(
                DESTINATION, Objects.requireNonNull(savedDriver.getId()), dto, EntityChangeType.UPDATED);
        return dto;
    }

    public void delete(Long id) {
        Driver driver = repositoryGuard.execute(
                () -> repository.findById(id).orElseThrow(() -> new NotFoundException(Driver.class, id)));
        repositoryGuard.execute(() -> repository.delete(driver));
        changePublisher.publish(DESTINATION, Objects.requireNonNull(driver.getId()), EntityChangeType.DELETED);
    }

    private Driver save(SaveDriverDto command, Driver driver) {
        mapper.update(driver, command);
        return repositoryGuard.execute(() -> repository.save(driver));
    }
}
