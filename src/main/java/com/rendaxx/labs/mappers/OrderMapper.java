package com.rendaxx.labs.mappers;

import com.rendaxx.labs.domain.Order;
import com.rendaxx.labs.domain.RoutePoint;
import com.rendaxx.labs.dtos.SaveOrderDto;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper
public abstract class OrderMapper {

    @Mapping(target = "id", ignore = true)
    public abstract void update(@MappingTarget Order order, SaveOrderDto dto);
}
