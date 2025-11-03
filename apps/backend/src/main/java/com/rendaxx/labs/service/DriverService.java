package com.rendaxx.labs.service;

import com.rendaxx.labs.domain.Driver;
import com.rendaxx.labs.dtos.DriverDto;
import com.rendaxx.labs.dtos.SaveDriverDto;
import com.rendaxx.labs.events.EntityChangePublisher;
import com.rendaxx.labs.events.EntityChangeType;
import com.rendaxx.labs.exceptions.NotFoundException;
import com.rendaxx.labs.mappers.DriverMapper;
import com.rendaxx.labs.repository.DriverRepository;
import com.rendaxx.labs.service.specification.EqualitySpecificationBuilder;
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

    private static final String DESTINATION = "/topic/drivers";

    public DriverDto create(SaveDriverDto command) {
        Driver driver = save(command, new Driver());
        DriverDto dto = mapper.toDto(driver);
        changePublisher.publish(DESTINATION, driver.getId(), dto, EntityChangeType.CREATED);
        return dto;
    }

    @Transactional(readOnly = true)
    public DriverDto getById(Long id) {
        Driver driver = repository.findById(id).orElseThrow(() -> new NotFoundException(Driver.class, id));
        return mapper.toDto(driver);
    }

    @Transactional(readOnly = true)
    public Page<DriverDto> getAll(Pageable pageable, Map<String, String> filters) {
        Specification<Driver> specification = specificationBuilder.build(filters);
        return repository.findAll(specification, pageable).map(mapper::toDto);
    }

    public DriverDto update(Long id, SaveDriverDto command) {
        Driver driver = repository.findById(id).orElseThrow(() -> new NotFoundException(Driver.class, id));
        Driver savedDriver = save(command, driver);
        DriverDto dto = mapper.toDto(savedDriver);
        changePublisher.publish(DESTINATION, savedDriver.getId(), dto, EntityChangeType.UPDATED);
        return dto;
    }

    public void delete(Long id) {
        Driver driver = repository.findById(id).orElseThrow(() -> new NotFoundException(Driver.class, id));
        repository.delete(driver);
        changePublisher.publish(DESTINATION, driver.getId(), null, EntityChangeType.DELETED);
    }

    private Driver save(SaveDriverDto command, Driver driver) {
        mapper.update(driver, command);
        return repository.save(driver);
    }
}
