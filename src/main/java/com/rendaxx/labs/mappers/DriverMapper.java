package com.rendaxx.labs.mappers;

import com.rendaxx.labs.domain.Driver;
import com.rendaxx.labs.dtos.SaveDriverDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper
public abstract class DriverMapper {

    @Mapping(target = "id", ignore = true)
    public abstract void update(@MappingTarget Driver driver, SaveDriverDto dto);
}
