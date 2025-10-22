package com.rendaxx.labs.dtos;

import com.rendaxx.labs.domain.OperationType;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoutePointDto {
    Long id;
    Long routeId;
    Long retailPointId;
    RetailPointDto retailPoint;
    OperationType operationType;
    Set<OrderDto> orders;
    LocalDateTime plannedStartTime;
    LocalDateTime plannedEndTime;
    Integer orderNumber;
}
