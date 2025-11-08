package com.rendaxx.labs.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SaveVehicleDto {
    Long driverId;

    @Size(min = 2)
    String gosNumber;

    @DecimalMin(value = "0.01")
    BigDecimal tonnageInTons;

    @DecimalMin(value = "0.01")
    BigDecimal bodyHeightInMeters;

    @DecimalMin(value = "0.01")
    BigDecimal bodyWidthInMeters;

    @DecimalMin(value = "0.01")
    BigDecimal bodyLengthInCubicMeters;
}
