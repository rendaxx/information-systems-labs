package com.rendaxx.labs.mappers;

import com.rendaxx.labs.domain.Driver;
import com.rendaxx.labs.domain.Vehicle;
import com.rendaxx.labs.dtos.SaveVehicleDto;
import com.rendaxx.labs.dtos.VehicleDto;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(uses = DriverMapper.class)
public abstract class VehicleMapper {

    @Mapping(target = "id", ignore = true)
    public abstract void update(
            @MappingTarget Vehicle vehicle, SaveVehicleDto dto, Driver driver);

    public abstract VehicleDto toDto(Vehicle vehicle);

    public abstract List<VehicleDto> toDto(List<Vehicle> vehicles);
}
