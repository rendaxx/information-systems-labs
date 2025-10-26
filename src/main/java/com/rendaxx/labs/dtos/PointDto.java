package com.rendaxx.labs.dtos;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PointDto {
    Double longitude;
    Double latitude;
}
