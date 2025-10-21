package com.rendaxx.labs.dtos;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SaveRouteDto {
    List<SaveRoutePointDto> routePoints;
    Long vehicleId;
    LocalDateTime creationTime;
    LocalDateTime plannedStartTime;
    LocalDateTime plannedEndTime;
    BigDecimal mileageInKm;
}
