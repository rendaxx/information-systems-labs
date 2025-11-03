package com.rendaxx.labs.dtos;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SaveVehicleDto {
    Long driverId;
    String gosNumber;
    BigDecimal tonnageInTons;
    BigDecimal bodyHeightInMeters;
    BigDecimal bodyWidthInMeters;
    BigDecimal bodyLengthInCubicMeters;
}
