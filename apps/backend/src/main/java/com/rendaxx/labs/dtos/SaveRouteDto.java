package com.rendaxx.labs.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SaveRouteDto {
    @NotNull
    List<SaveRoutePointDto> routePoints;

    @NotNull
    Long vehicleId;

    @NotNull
    LocalDateTime creationTime;

    @NotNull
    LocalDateTime plannedStartTime;

    @NotNull
    LocalDateTime plannedEndTime;

    @NotNull
    @DecimalMin(value = "0.001")
    BigDecimal mileageInKm;
}
