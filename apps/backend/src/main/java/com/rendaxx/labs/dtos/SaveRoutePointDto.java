package com.rendaxx.labs.dtos;

import com.rendaxx.labs.domain.OperationType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.Nullable;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SaveRoutePointDto {
    @Nullable
    Long id;

    @Nullable
    Long routeId;

    @NotNull
    Long retailPointId;

    @NotNull
    OperationType operationType;

    @NotNull
    List<Long> orderIds;

    @NotNull
    LocalDateTime plannedStartTime;

    @NotNull
    LocalDateTime plannedEndTime;

    @NotNull
    @PositiveOrZero
    Long orderNumber;
}
