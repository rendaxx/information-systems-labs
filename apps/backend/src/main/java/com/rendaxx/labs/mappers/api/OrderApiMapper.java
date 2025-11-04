package com.rendaxx.labs.mappers.api;

import com.rendaxx.labs.api.v1.model.OrderApiDto;
import com.rendaxx.labs.api.v1.model.PageOrderApiDto;
import com.rendaxx.labs.api.v1.model.SaveOrderApiDto;
import com.rendaxx.labs.dtos.OrderDto;
import com.rendaxx.labs.dtos.SaveOrderDto;
import com.rendaxx.labs.mappers.api.support.PageSortMapper;
import java.util.List;
import java.util.Set;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

@Mapper
public interface OrderApiMapper extends PageSortMapper {

    OrderApiDto toApi(OrderDto dto);

    List<OrderApiDto> toApi(List<OrderDto> dto);

    List<OrderApiDto> toApi(Set<OrderDto> dto);

    SaveOrderDto toDto(SaveOrderApiDto dto);

    @Mapping(target = "page", source = "number")
    PageOrderApiDto toOrderPage(Page<OrderDto> page);
}
