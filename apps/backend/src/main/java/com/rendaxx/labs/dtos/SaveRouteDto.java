package com.rendaxx.labs.dtos;

import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SaveRouteDto {
    List<SaveRoutePointDto> routePoints;
    Long vehicleId;
    LocalDateTime creationTime;
    LocalDateTime plannedStartTime;
    LocalDateTime plannedEndTime;

    @DecimalMin(value = "0.001")
    BigDecimal mileageInKm;
}
