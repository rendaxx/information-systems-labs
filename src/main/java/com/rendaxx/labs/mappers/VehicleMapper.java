package com.rendaxx.labs.mappers;

import com.rendaxx.labs.domain.Driver;
import com.rendaxx.labs.domain.Vehicle;
import com.rendaxx.labs.dtos.SaveVehicleDto;
import com.rendaxx.labs.exceptions.NotFoundException;
import com.rendaxx.labs.repository.DriverRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper
public abstract class VehicleMapper {

    @Autowired
    DriverRepository driverRepository;

    @Mapping(target = "id", ignore = true)
    public abstract void update(@MappingTarget Vehicle vehicle, SaveVehicleDto dto, Driver driver);

    public void update(Vehicle vehicle, SaveVehicleDto dto) {
        Driver driver = driverRepository
                .findById(dto.getDriverId())
                .orElseThrow(() -> new NotFoundException(Driver.class, dto.getDriverId()));
        update(vehicle, dto, driver);
    }
}
