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

import java.util.List;

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
