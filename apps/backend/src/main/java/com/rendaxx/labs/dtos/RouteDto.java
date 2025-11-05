package com.rendaxx.labs.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RouteDto {
    Long id;
    List<RoutePointDto> routePoints;
    VehicleDto vehicle;
    LocalDateTime creationTime;
    LocalDateTime plannedStartTime;
    LocalDateTime plannedEndTime;
    BigDecimal mileageInKm;
}
