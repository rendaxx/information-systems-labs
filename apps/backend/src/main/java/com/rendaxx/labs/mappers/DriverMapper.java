package com.rendaxx.labs.mappers;

import com.rendaxx.labs.domain.Driver;
import com.rendaxx.labs.dtos.DriverDto;
import com.rendaxx.labs.dtos.SaveDriverDto;
import com.rendaxx.labs.repository.view.DriverView;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper
public abstract class DriverMapper {

    @Mapping(target = "id", ignore = true)
    public abstract void update(@MappingTarget Driver driver, SaveDriverDto dto);

    public abstract DriverDto toDto(Driver driver);

    public abstract DriverDto toDto(DriverView driver);

    public abstract List<DriverDto> toDto(List<Driver> drivers);

    public abstract List<DriverDto> toDtoFromView(List<DriverView> drivers);
}
