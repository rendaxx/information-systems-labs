package com.rendaxx.labs.mappers;

import com.rendaxx.labs.domain.RetailPoint;
import com.rendaxx.labs.dtos.SaveRetailPointDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper
public abstract class RetailPointMapper {

    @Mapping(target = "id", ignore = true)
    public abstract void update(@MappingTarget RetailPoint retailPoint, SaveRetailPointDto dto);
}
