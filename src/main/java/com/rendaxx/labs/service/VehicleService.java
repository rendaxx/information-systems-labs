package com.rendaxx.labs.service;

import com.rendaxx.labs.domain.Vehicle;
import com.rendaxx.labs.dtos.SaveVehicleDto;
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

    public Vehicle create(SaveVehicleDto command) {
        return save(command, new Vehicle());
    }

    @Transactional(readOnly = true)
    public Vehicle getById(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException(Vehicle.class, id));
    }

    @Transactional(readOnly = true)
    public List<Vehicle> getAll() {
        return repository.findAll();
    }

    public Vehicle update(Long id, SaveVehicleDto command) {
        Vehicle vehicle = repository.findById(id).orElseThrow(() -> new NotFoundException(Vehicle.class, id));
        return save(command, vehicle);
    }

    public void delete(Long id) {
        Vehicle vehicle = repository.findById(id).orElseThrow(() -> new NotFoundException(Vehicle.class, id));
        repository.delete(vehicle);
    }

    private Vehicle save(SaveVehicleDto command, Vehicle vehicle) {
        mapper.update(vehicle, command);
        return repository.save(vehicle);
    }
}
