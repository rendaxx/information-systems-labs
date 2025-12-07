package com.rendaxx.labs.mappers;

import com.rendaxx.labs.domain.RetailPoint;
import com.rendaxx.labs.dtos.PointDto;
import com.rendaxx.labs.dtos.RetailPointDto;
import com.rendaxx.labs.dtos.SaveRetailPointDto;
import com.rendaxx.labs.repository.view.RetailPointView;
import java.util.List;
import org.locationtech.jts.geom.Point;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper
public abstract class RetailPointMapper {

    @Mapping(target = "id", ignore = true)
    public abstract void update(@MappingTarget RetailPoint retailPoint, SaveRetailPointDto dto);

    public abstract RetailPointDto toDto(RetailPoint retailPoint);

    public abstract RetailPointDto toDto(RetailPointView retailPoint);

    public abstract List<RetailPointDto> toDto(List<RetailPoint> retailPoints);

    public abstract List<RetailPointDto> toDtoFromView(List<RetailPointView> retailPoints);

    protected PointDto map(Point location) {
        PointDto dto = new PointDto();
        dto.setLongitude(location.getX());
        dto.setLatitude(location.getY());
        return dto;
    }
}
