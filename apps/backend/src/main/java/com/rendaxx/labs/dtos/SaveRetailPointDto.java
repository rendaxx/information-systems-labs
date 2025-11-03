package com.rendaxx.labs.dtos;

import com.rendaxx.labs.domain.PointType;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.locationtech.jts.geom.Point;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SaveRetailPointDto {
    String name;
    String address;
    Point location;
    PointType type;
    String timezone;
}
