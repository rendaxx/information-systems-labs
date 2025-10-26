package com.rendaxx.labs.mappers;

import com.rendaxx.labs.domain.RetailPoint;
import com.rendaxx.labs.dtos.PointDto;
import com.rendaxx.labs.dtos.RetailPointDto;
import com.rendaxx.labs.dtos.SaveRetailPointDto;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.locationtech.jts.geom.Point;

@Mapper
public abstract class RetailPointMapper {

    @Mapping(target = "id", ignore = true)
    public abstract void update(@MappingTarget RetailPoint retailPoint, SaveRetailPointDto dto);

    public abstract RetailPointDto toDto(RetailPoint retailPoint);

    public abstract List<RetailPointDto> toDto(List<RetailPoint> retailPoints);

    protected PointDto map(Point location) {
        if (location == null) {
            return null;
        }
        PointDto dto = new PointDto();
        dto.setLongitude(location.getX());
        dto.setLatitude(location.getY());
        return dto;
    }
}
