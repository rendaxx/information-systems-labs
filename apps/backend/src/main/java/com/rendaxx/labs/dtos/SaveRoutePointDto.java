package com.rendaxx.labs.dtos;

import com.rendaxx.labs.domain.OperationType;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SaveRoutePointDto {
    Long id;
    Long routeId;
    Long retailPointId;
    OperationType operationType;
    List<Long> orderIds;
    LocalDateTime plannedStartTime;
    LocalDateTime plannedEndTime;
    Integer orderNumber;
}
