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
import com.rendaxx.labs.service.specification.EqualitySpecificationBuilder;
import java.util.Objects;
import java.util.Map;
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
        DriverDto dto = mapper.toDto(driver);
        changePublisher.publish(DESTINATION, Objects.requireNonNull(driver.getId()), dto, EntityChangeType.CREATED);
        return dto;
    }

    @Transactional(readOnly = true)
    public DriverDto getById(Long id) {
        Driver driver = repositoryGuard.execute(
                () -> repository.findById(id).orElseThrow(() -> new NotFoundException(Driver.class, id)));
        return mapper.toDto(driver);
    }

    @Transactional(readOnly = true)
    public Page<DriverDto> getAll(Pageable pageable, Map<String, String> filters) {
        Specification<Driver> specification = specificationBuilder.build(filters);
        Page<Driver> drivers = repositoryGuard.execute(() -> repository.findAll(specification, pageable));
        return drivers.map(mapper::toDto);
    }

    public DriverDto update(Long id, SaveDriverDto command) {
        Driver driver = repositoryGuard.execute(
                () -> repository.findById(id).orElseThrow(() -> new NotFoundException(Driver.class, id)));
        Driver savedDriver = save(command, driver);
        DriverDto dto = mapper.toDto(savedDriver);
        changePublisher.publish(
                DESTINATION, Objects.requireNonNull(savedDriver.getId()), dto, EntityChangeType.UPDATED);
        return dto;
    }

    public void delete(Long id) {
        Driver driver = repositoryGuard.execute(
                () -> repository.findById(id).orElseThrow(() -> new NotFoundException(Driver.class, id)));
        repositoryGuard.execute(() -> repository.delete(driver));
        changePublisher.publish(DESTINATION, Objects.requireNonNull(driver.getId()), null, EntityChangeType.DELETED);
    }

    private Driver save(SaveDriverDto command, Driver driver) {
        mapper.update(driver, command);
        return repositoryGuard.execute(() -> repository.save(driver));
    }
}
