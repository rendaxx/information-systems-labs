package com.rendaxx.labs.mappers.api;

import com.rendaxx.labs.api.v1.model.PageVehicleApiDto;
import com.rendaxx.labs.api.v1.model.SaveVehicleApiDto;
import com.rendaxx.labs.api.v1.model.VehicleApiDto;
import com.rendaxx.labs.dtos.SaveVehicleDto;
import com.rendaxx.labs.dtos.VehicleDto;
import com.rendaxx.labs.mappers.api.support.PageSortMapper;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

@Mapper(uses = DriverApiMapper.class)
public interface VehicleApiMapper extends PageSortMapper {

    VehicleApiDto toApi(VehicleDto dto);

    List<VehicleApiDto> toApi(List<VehicleDto> dto);

    SaveVehicleDto toDto(SaveVehicleApiDto dto);

    @Mapping(target = "page", source = "number")
    @Mapping(target = "sort", expression = "java(toSortStrings(page.getSort()))")
    PageVehicleApiDto toVehiclePage(Page<VehicleDto> page);
}
