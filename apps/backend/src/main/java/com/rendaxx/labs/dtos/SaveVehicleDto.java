package com.rendaxx.labs.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SaveVehicleDto {
    @NotNull
    Long driverId;

    @NotBlank
    @Size(min = 2)
    String gosNumber;

    @NotNull
    @DecimalMin(value = "0.01")
    BigDecimal tonnageInTons;

    @NotNull
    @DecimalMin(value = "0.01")
    BigDecimal bodyHeightInMeters;

    @NotNull
    @DecimalMin(value = "0.01")
    BigDecimal bodyWidthInMeters;

    @NotNull
    @DecimalMin(value = "0.01")
    BigDecimal bodyLengthInCubicMeters;
}
