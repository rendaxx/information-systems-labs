package com.rendaxx.labs.dtos;

import com.rendaxx.labs.domain.PointType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.locationtech.jts.geom.Point;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SaveRetailPointDto {
    @NotBlank
    String name;

    @NotBlank
    String address;

    @NotNull
    Point location;

    @NotNull
    PointType type;

    @NotBlank
    String timezone;
}
