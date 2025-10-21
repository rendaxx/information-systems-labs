package com.rendaxx.labs.domain;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
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

    @NotEmpty
    @Valid
    @Builder.Default
    @OneToMany(mappedBy = "routePoint", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<Order> orders = new ArrayList<>();

    @NotNull
    private LocalDateTime plannedStartTime;

    @NotNull
    private LocalDateTime plannedEndTime;

    @NotNull
    @PositiveOrZero
    private Integer orderNumber;
}
