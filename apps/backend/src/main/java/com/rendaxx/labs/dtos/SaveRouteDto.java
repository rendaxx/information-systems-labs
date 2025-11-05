package com.rendaxx.labs.dtos;

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
    BigDecimal mileageInKm;
}
