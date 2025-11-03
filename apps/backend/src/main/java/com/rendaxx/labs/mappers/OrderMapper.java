package com.rendaxx.labs.mappers;

import com.rendaxx.labs.domain.Order;
import com.rendaxx.labs.dtos.OrderDto;
import com.rendaxx.labs.dtos.SaveOrderDto;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper
public abstract class OrderMapper {

    @Mapping(target = "id", ignore = true)
    public abstract void update(@MappingTarget Order order, SaveOrderDto dto);

    public abstract OrderDto toDto(Order order);

    public abstract List<OrderDto> toDto(List<Order> orders);
}
