package com.rendaxx.labs.repository.view;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface RouteView {
    Long getId();

    List<RoutePointView> getRoutePoints();

    VehicleView getVehicle();

    LocalDateTime getCreationTime();

    LocalDateTime getPlannedStartTime();

    LocalDateTime getPlannedEndTime();

    BigDecimal getMileageInKm();
}
