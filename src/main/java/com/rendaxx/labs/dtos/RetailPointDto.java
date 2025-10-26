package com.rendaxx.labs.dtos;

import com.rendaxx.labs.domain.PointType;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RetailPointDto {
    Long id;
    String name;
    String address;
    PointDto location;
    PointType type;
    String timezone;
}
