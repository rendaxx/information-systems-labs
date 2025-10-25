package com.rendaxx.labs.mappers;

import com.rendaxx.labs.domain.Driver;
import com.rendaxx.labs.domain.Vehicle;
import com.rendaxx.labs.dtos.SaveVehicleDto;
import com.rendaxx.labs.dtos.VehicleDto;
import com.rendaxx.labs.exceptions.NotFoundException;
import com.rendaxx.labs.repository.DriverRepository;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(uses = DriverMapper.class)
public abstract class VehicleMapper {

    @Autowired
    private DriverRepository driverRepository;

    @Mapping(target = "id", ignore = true)
    abstract void update(@MappingTarget Vehicle vehicle, SaveVehicleDto dto, Driver driver);

    public void update(Vehicle vehicle, SaveVehicleDto dto) {
        Driver driver = driverRepository
                .findById(dto.getDriverId())
                .orElseThrow(() -> new NotFoundException(Driver.class, dto.getDriverId()));
        update(vehicle, dto, driver);
    }

    @Mapping(target = "driverId", source = "driver.id")
    public abstract VehicleDto toDto(Vehicle vehicle);

    public abstract List<VehicleDto> toDto(List<Vehicle> vehicles);
}
