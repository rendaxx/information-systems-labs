package com.rendaxx.labs.repository.view;

import java.math.BigDecimal;

public interface VehicleView {
    Long getId();

    DriverView getDriver();

    String getGosNumber();

    BigDecimal getTonnageInTons();

    BigDecimal getBodyHeightInMeters();

    BigDecimal getBodyWidthInMeters();

    BigDecimal getBodyLengthInCubicMeters();
}
