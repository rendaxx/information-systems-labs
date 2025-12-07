package com.rendaxx.labs.service;

import com.rendaxx.labs.domain.Driver;
import com.rendaxx.labs.domain.Vehicle;
import com.rendaxx.labs.dtos.SaveVehicleDto;
import com.rendaxx.labs.dtos.VehicleDto;
import com.rendaxx.labs.events.EntityChangePublisher;
import com.rendaxx.labs.events.EntityChangeType;
import com.rendaxx.labs.exceptions.NotFoundException;
import com.rendaxx.labs.mappers.VehicleMapper;
import com.rendaxx.labs.repository.DriverRepository;
import com.rendaxx.labs.repository.VehicleRepository;
import com.rendaxx.labs.repository.support.RepositoryGuard;
import com.rendaxx.labs.repository.view.VehicleView;
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
public class VehicleService {

    VehicleMapper mapper;
    VehicleRepository repository;
    DriverRepository driverRepository;
    EntityChangePublisher changePublisher;
    EqualitySpecificationBuilder specificationBuilder;
    RepositoryGuard repositoryGuard;

    private static final String DESTINATION = "/topic/vehicles";

    public VehicleDto create(SaveVehicleDto command) {
        Vehicle vehicle = save(command, new Vehicle());
        VehicleDto dto = repositoryGuard.execute(() -> repository
                .findViewById(Objects.requireNonNull(vehicle.getId()))
                .map(mapper::toDto)
                .orElseThrow(() -> new NotFoundException(Vehicle.class, vehicle.getId())));
        changePublisher.publish(DESTINATION, Objects.requireNonNull(vehicle.getId()), dto, EntityChangeType.CREATED);
        return dto;
    }

    @Transactional(readOnly = true)
    public VehicleDto getById(Long id) {
        return repositoryGuard.execute(() -> repository
                .findViewById(id)
                .map(mapper::toDto)
                .orElseThrow(() -> new NotFoundException(Vehicle.class, id)));
    }

    @Transactional(readOnly = true)
    public Page<VehicleDto> getAll(Pageable pageable, Map<String, String> filters) {
        Specification<Vehicle> specification = specificationBuilder.build(filters);

        return repositoryGuard
                .execute(() -> repository.findBy(
                        specification, q -> q.as(VehicleView.class).page(pageable)))
                .map(mapper::toDto);
    }

    public VehicleDto update(Long id, SaveVehicleDto command) {
        Vehicle vehicle = repositoryGuard.execute(
                () -> repository.findById(id).orElseThrow(() -> new NotFoundException(Vehicle.class, id)));
        Vehicle savedVehicle = save(command, vehicle);
        VehicleDto dto = repositoryGuard.execute(() -> repository
                .findViewById(id)
                .map(mapper::toDto)
                .orElseThrow(() -> new NotFoundException(Vehicle.class, id)));
        changePublisher.publish(
                DESTINATION, Objects.requireNonNull(savedVehicle.getId()), dto, EntityChangeType.UPDATED);
        return dto;
    }

    public void delete(Long id) {
        Vehicle vehicle = repositoryGuard.execute(
                () -> repository.findById(id).orElseThrow(() -> new NotFoundException(Vehicle.class, id)));
        repositoryGuard.execute(() -> repository.delete(vehicle));
        changePublisher.publish(DESTINATION, Objects.requireNonNull(vehicle.getId()), null, EntityChangeType.DELETED);
    }

    private Vehicle save(SaveVehicleDto command, Vehicle vehicle) {
        Driver driver = repositoryGuard.execute(() -> driverRepository
                .findById(command.getDriverId())
                .orElseThrow(() -> new NotFoundException(Driver.class, command.getDriverId())));
        mapper.update(vehicle, command, driver);
        return repositoryGuard.execute(() -> repository.save(vehicle));
    }
}
