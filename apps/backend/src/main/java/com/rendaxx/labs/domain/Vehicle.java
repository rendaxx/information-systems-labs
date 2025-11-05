package com.rendaxx.labs.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@Table(
        name = "vehicles",
        uniqueConstraints = {@UniqueConstraint(name = "uk_vehicles_gos_number", columnNames = "gos_number")})
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Driver driver;

    @NotBlank
    @Column(unique = true)
    private String gosNumber;

    @NotNull
    @Positive
    @Column(precision = 8, scale = 2)
    private BigDecimal tonnageInTons;

    @NotNull
    @Positive
    @Column(precision = 8, scale = 2)
    private BigDecimal bodyHeightInMeters;

    @NotNull
    @Positive
    @Column(precision = 8, scale = 2)
    private BigDecimal bodyWidthInMeters;

    @NotNull
    @Positive
    @Column(precision = 8, scale = 2)
    private BigDecimal bodyLengthInCubicMeters;
}
