package com.rendaxx.labs.repository.view;

import java.math.BigDecimal;

public interface OrderView {
    Long getId();

    String getGoodsType();

    Integer getMinTemperature();

    Integer getMaxTemperature();

    BigDecimal getVolumeInCubicMeters();

    BigDecimal getWeightInKg();
}
