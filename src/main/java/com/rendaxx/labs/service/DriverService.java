package com.rendaxx.labs.service;

import com.rendaxx.labs.domain.Driver;
import com.rendaxx.labs.dtos.DriverDto;
import com.rendaxx.labs.dtos.SaveDriverDto;
import com.rendaxx.labs.exceptions.NotFoundException;
import com.rendaxx.labs.mappers.DriverMapper;
import com.rendaxx.labs.repository.DriverRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class DriverService {

    DriverMapper mapper;
    DriverRepository repository;

    public DriverDto create(SaveDriverDto command) {
        Driver driver = save(command, new Driver());
        return mapper.toDto(driver);
    }

    @Transactional(readOnly = true)
    public DriverDto getById(Long id) {
        Driver driver = repository.findById(id).orElseThrow(() -> new NotFoundException(Driver.class, id));
        return mapper.toDto(driver);
    }

    @Transactional(readOnly = true)
    public List<DriverDto> getAll() {
        return mapper.toDto(repository.findAll());
    }

    public DriverDto update(Long id, SaveDriverDto command) {
        Driver driver = repository.findById(id).orElseThrow(() -> new NotFoundException(Driver.class, id));
        Driver savedDriver = save(command, driver);
        return mapper.toDto(savedDriver);
    }

    public void delete(Long id) {
        Driver driver = repository.findById(id).orElseThrow(() -> new NotFoundException(Driver.class, id));
        repository.delete(driver);
    }

    private Driver save(SaveDriverDto command, Driver driver) {
        mapper.update(driver, command);
        return repository.save(driver);
    }
}
