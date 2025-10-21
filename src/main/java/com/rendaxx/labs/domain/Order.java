package com.rendaxx.labs.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String goodsType;

    private Integer minTemperature;

    private Integer maxTemperature;

    @NotNull
    @Positive
    @Column(precision = 12, scale = 3)
    private BigDecimal volumeInCubicMeters;

    @NotNull
    @Positive
    @Column(precision = 12, scale = 3)
    private BigDecimal weightInKg;

    @AssertTrue(message = "max_temperature must be greater than or equal to min_temperature")
    public boolean isTemperatureRangeValid() {
        if (minTemperature == null || maxTemperature == null) {
            return true;
        }
        return maxTemperature >= minTemperature;
    }
}
