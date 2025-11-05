package com.rendaxx.labs.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
