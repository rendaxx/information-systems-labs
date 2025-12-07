package com.rendaxx.labs.repository.view;

import com.rendaxx.labs.domain.OperationType;
import java.time.LocalDateTime;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;

public interface RoutePointView {
    Long getId();

    @Value("#{target.route.id}")
    Long getRouteId();

    RetailPointView getRetailPoint();

    OperationType getOperationType();

    Set<OrderView> getOrders();

    LocalDateTime getPlannedStartTime();

    LocalDateTime getPlannedEndTime();

    Integer getOrderNumber();
}
