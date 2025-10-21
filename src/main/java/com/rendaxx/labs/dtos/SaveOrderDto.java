package com.rendaxx.labs.dtos;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SaveOrderDto {
    String goodsType;
    Integer minTemperature;
    Integer maxTemperature;
    BigDecimal volumeInCubicMeters;
    BigDecimal weightInKg;
}
