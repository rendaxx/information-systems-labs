package com.rendaxx.labs.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
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
@Table(name = "vehicles")
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Driver driver;

    @NotBlank
    @Size(min = 2)
    @Column(unique = true)
    private String gosNumber;

    @NotNull
    @Positive
    @DecimalMin(value = "0.01")
    @Column(precision = 8, scale = 2)
    private BigDecimal tonnageInTons;

    @NotNull
    @Positive
    @DecimalMin(value = "0.01")
    @Column(precision = 8, scale = 2)
    private BigDecimal bodyHeightInMeters;

    @NotNull
    @Positive
    @DecimalMin(value = "0.01")
    @Column(precision = 8, scale = 2)
    private BigDecimal bodyWidthInMeters;

    @NotNull
    @Positive
    @DecimalMin(value = "0.01")
    @Column(precision = 8, scale = 2)
    private BigDecimal bodyLengthInCubicMeters;
}
