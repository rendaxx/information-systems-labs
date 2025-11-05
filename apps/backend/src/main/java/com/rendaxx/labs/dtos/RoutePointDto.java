package com.rendaxx.labs.dtos;

import com.rendaxx.labs.domain.OperationType;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoutePointDto {
    Long id;
    Long routeId;
    RetailPointDto retailPoint;
    OperationType operationType;
    Set<OrderDto> orders;
    LocalDateTime plannedStartTime;
    LocalDateTime plannedEndTime;
    Integer orderNumber;
}
