package com.rendaxx.labs.dtos;

import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

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
