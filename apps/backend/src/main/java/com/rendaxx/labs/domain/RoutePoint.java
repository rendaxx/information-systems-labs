package com.rendaxx.labs.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
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
        name = "route_points",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_route_points_route_order",
                    columnNames = {"route_id", "order_number"})
        })
public class RoutePoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Route route;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private RetailPoint retailPoint;

    @NotNull
    @Enumerated(EnumType.STRING)
    private OperationType operationType;

    @Valid
    @Builder.Default
    @ManyToMany(
            fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "route_point_orders",
            joinColumns = @JoinColumn(name = "route_point_id"),
            inverseJoinColumns = @JoinColumn(name = "order_id"),
            uniqueConstraints =
                    @UniqueConstraint(
                            name = "uk_route_point_order_unique",
                            columnNames = {"route_point_id", "order_id"}))
    private Set<Order> orders = new HashSet<>();

    @NotNull
    private LocalDateTime plannedStartTime;

    @NotNull
    private LocalDateTime plannedEndTime;

    @NotNull
    @PositiveOrZero
    private Integer orderNumber;
}
