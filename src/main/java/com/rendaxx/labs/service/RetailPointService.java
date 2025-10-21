package com.rendaxx.labs.service;

import com.rendaxx.labs.domain.RetailPoint;
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

    public RetailPoint create(SaveRetailPointDto command) {
        return save(command, new RetailPoint());
    }

    @Transactional(readOnly = true)
    public RetailPoint getById(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException(RetailPoint.class, id));
    }

    @Transactional(readOnly = true)
    public List<RetailPoint> getAll() {
        return repository.findAll();
    }

    public RetailPoint update(Long id, SaveRetailPointDto command) {
        RetailPoint retailPoint = repository.findById(id).orElseThrow(() -> new NotFoundException(RetailPoint.class, id));
        return save(command, retailPoint);
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
