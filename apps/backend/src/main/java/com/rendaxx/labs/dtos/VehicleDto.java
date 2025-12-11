package com.rendaxx.labs.dtos;

import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.Nullable;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VehicleDto {
    Long id;

    @Nullable
    DriverDto driver;

    String gosNumber;
    BigDecimal tonnageInTons;
    BigDecimal bodyHeightInMeters;
    BigDecimal bodyWidthInMeters;
    BigDecimal bodyLengthInCubicMeters;
}
