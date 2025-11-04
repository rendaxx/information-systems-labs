package com.rendaxx.labs.mappers.api;

import com.rendaxx.labs.api.v1.model.SaveVehicleApiDto;
import com.rendaxx.labs.api.v1.model.VehicleApiDto;
import com.rendaxx.labs.dtos.SaveVehicleDto;
import com.rendaxx.labs.dtos.VehicleDto;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(uses = DriverApiMapper.class)
public interface VehicleApiMapper {

    VehicleApiDto toApi(VehicleDto dto);

    List<VehicleApiDto> toApi(List<VehicleDto> dto);

    SaveVehicleDto toDto(SaveVehicleApiDto dto);
}
