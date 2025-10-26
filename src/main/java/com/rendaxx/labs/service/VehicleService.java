package com.rendaxx.labs.service;

import com.rendaxx.labs.domain.Vehicle;
import com.rendaxx.labs.dtos.SaveVehicleDto;
import com.rendaxx.labs.dtos.VehicleDto;
import com.rendaxx.labs.events.EntityChangePublisher;
import com.rendaxx.labs.events.EntityChangeType;
import com.rendaxx.labs.exceptions.NotFoundException;
import com.rendaxx.labs.mappers.VehicleMapper;
import com.rendaxx.labs.repository.VehicleRepository;
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
public class VehicleService {

    VehicleMapper mapper;
    VehicleRepository repository;
    EntityChangePublisher changePublisher;

    private static final String DESTINATION = "/topic/vehicles";

    public VehicleDto create(SaveVehicleDto command) {
        Vehicle vehicle = save(command, new Vehicle());
        VehicleDto dto = mapper.toDto(vehicle);
        changePublisher.publish(DESTINATION, vehicle.getId(), dto, EntityChangeType.CREATED);
        return dto;
    }

    @Transactional(readOnly = true)
    public VehicleDto getById(Long id) {
        Vehicle vehicle = repository.findById(id).orElseThrow(() -> new NotFoundException(Vehicle.class, id));
        return mapper.toDto(vehicle);
    }

    @Transactional(readOnly = true)
    public List<VehicleDto> getAll() {
        return mapper.toDto(repository.findAll());
    }

    public VehicleDto update(Long id, SaveVehicleDto command) {
        Vehicle vehicle = repository.findById(id).orElseThrow(() -> new NotFoundException(Vehicle.class, id));
        Vehicle savedVehicle = save(command, vehicle);
        VehicleDto dto = mapper.toDto(savedVehicle);
        changePublisher.publish(DESTINATION, savedVehicle.getId(), dto, EntityChangeType.UPDATED);
        return dto;
    }

    public void delete(Long id) {
        Vehicle vehicle = repository.findById(id).orElseThrow(() -> new NotFoundException(Vehicle.class, id));
        repository.delete(vehicle);
        changePublisher.publish(DESTINATION, vehicle.getId(), null, EntityChangeType.DELETED);
    }

    private Vehicle save(SaveVehicleDto command, Vehicle vehicle) {
        mapper.update(vehicle, command);
        return repository.save(vehicle);
    }
}
