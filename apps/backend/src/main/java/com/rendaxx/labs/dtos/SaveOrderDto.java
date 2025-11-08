package com.rendaxx.labs.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SaveOrderDto {
    @NotBlank
    @Pattern(regexp = "^(?!.*\\u0000).+$")
    String goodsType;

    Integer minTemperature;
    Integer maxTemperature;

    @DecimalMin(value = "0.001")
    BigDecimal volumeInCubicMeters;

    @DecimalMin(value = "0.001")
    BigDecimal weightInKg;
}
