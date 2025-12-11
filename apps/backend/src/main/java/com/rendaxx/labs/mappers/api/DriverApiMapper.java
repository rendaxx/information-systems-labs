package com.rendaxx.labs.mappers.api;

import com.rendaxx.labs.api.v1.model.DriverApiDto;
import com.rendaxx.labs.api.v1.model.PageDriverApiDto;
import com.rendaxx.labs.api.v1.model.SaveDriverApiDto;
import com.rendaxx.labs.dtos.DriverDto;
import com.rendaxx.labs.dtos.SaveDriverDto;
import com.rendaxx.labs.mappers.api.support.JsonNullableMapper;
import com.rendaxx.labs.mappers.api.support.PageSortMapper;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

@Mapper
public interface DriverApiMapper extends JsonNullableMapper, PageSortMapper {

    DriverApiDto toApi(DriverDto dto);

    List<DriverApiDto> toApi(List<DriverDto> dto);

    SaveDriverDto toDto(SaveDriverApiDto dto);

    @Mapping(target = "page", source = "number")
    PageDriverApiDto toDriverPage(Page<DriverDto> page);
}
