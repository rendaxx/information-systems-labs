package com.rendaxx.labs.mappers.api;

import com.rendaxx.labs.api.v1.model.DriverApiDto;
import com.rendaxx.labs.api.v1.model.SaveDriverApiDto;
import com.rendaxx.labs.dtos.DriverDto;
import com.rendaxx.labs.dtos.SaveDriverDto;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.openapitools.jackson.nullable.JsonNullable;

@Mapper
public interface DriverApiMapper {

    @Mapping(target = "middleName", expression = "java(toNullable(dto.getMiddleName()))")
    DriverApiDto toApi(DriverDto dto);

    List<DriverApiDto> toApi(List<DriverDto> dto);

    @Mapping(target = "middleName", expression = "java(fromNullable(dto.getMiddleName()))")
    SaveDriverDto toDto(SaveDriverApiDto dto);

    default JsonNullable<String> toNullable(String value) {
        if (value == null) {
            return JsonNullable.undefined();
        }
        return JsonNullable.of(value);
    }

    default String fromNullable(JsonNullable<String> value) {
        if (value == null || !value.isPresent()) {
            return null;
        }
        return value.orElse(null);
    }
}
