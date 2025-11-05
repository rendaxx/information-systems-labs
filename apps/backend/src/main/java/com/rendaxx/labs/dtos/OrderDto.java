package com.rendaxx.labs.dtos;

import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderDto {
    Long id;
    String goodsType;
    Integer minTemperature;
    Integer maxTemperature;
    BigDecimal volumeInCubicMeters;
    BigDecimal weightInKg;
}
