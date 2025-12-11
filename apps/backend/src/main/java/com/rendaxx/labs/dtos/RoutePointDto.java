package com.rendaxx.labs.dtos;

import com.rendaxx.labs.domain.OperationType;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.Nullable;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoutePointDto {
    Long id;

    @Nullable
    Long routeId;

    RetailPointDto retailPoint;
    OperationType operationType;
    Set<OrderDto> orders;
    LocalDateTime plannedStartTime;
    LocalDateTime plannedEndTime;
    Integer orderNumber;
}
