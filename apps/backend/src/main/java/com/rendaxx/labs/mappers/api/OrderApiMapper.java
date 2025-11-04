package com.rendaxx.labs.mappers.api;

import com.rendaxx.labs.api.v1.model.OrderApiDto;
import com.rendaxx.labs.api.v1.model.SaveOrderApiDto;
import com.rendaxx.labs.dtos.OrderDto;
import com.rendaxx.labs.dtos.SaveOrderDto;
import java.util.List;
import java.util.Set;
import org.mapstruct.Mapper;

@Mapper
public interface OrderApiMapper {

    OrderApiDto toApi(OrderDto dto);

    List<OrderApiDto> toApi(List<OrderDto> dto);

    List<OrderApiDto> toApi(Set<OrderDto> dto);

    SaveOrderDto toDto(SaveOrderApiDto dto);
}
