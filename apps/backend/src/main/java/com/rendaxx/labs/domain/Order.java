package com.rendaxx.labs.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

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
    private @Nullable Long id;

    @NotBlank
    @Pattern(regexp = "^(?!.*\\u0000).+$", message = "must not contain null characters")
    private String goodsType;

    @Nullable
    @Min(value = -100, message = "must not be lower than -100°C")
    @Max(value = 200, message = "must not exceed 200°C")
    private Integer minTemperature;

    @Nullable
    @Min(value = -100, message = "must not be lower than -100°C")
    @Max(value = 200, message = "must not exceed 200°C")
    private Integer maxTemperature;

    @NotNull
    @Positive
    @DecimalMin(value = "0.001")
    @Column(precision = 12, scale = 3)
    private BigDecimal volumeInCubicMeters;

    @NotNull
    @Positive
    @DecimalMin(value = "0.001")
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
