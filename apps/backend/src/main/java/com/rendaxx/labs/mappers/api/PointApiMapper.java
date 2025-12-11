package com.rendaxx.labs.mappers.api;

import com.rendaxx.labs.api.v1.model.PointApiDto;
import com.rendaxx.labs.dtos.PointDto;
import org.mapstruct.Mapper;

@Mapper
public interface PointApiMapper {

    PointApiDto toApi(PointDto dto);

    PointDto toDto(PointApiDto dto);
}
