package com.rendaxx.labs.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "vehicles", uniqueConstraints = {@UniqueConstraint(name = "uk_vehicles_gos_number", columnNames = "gos_number")})
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
