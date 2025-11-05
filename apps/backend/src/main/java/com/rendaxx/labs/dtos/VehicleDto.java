package com.rendaxx.labs.dtos;

import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VehicleDto {
    Long id;
    DriverDto driver;
    String gosNumber;
    BigDecimal tonnageInTons;
    BigDecimal bodyHeightInMeters;
    BigDecimal bodyWidthInMeters;
    BigDecimal bodyLengthInCubicMeters;
}
