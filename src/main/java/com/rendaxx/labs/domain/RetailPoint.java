package com.rendaxx.labs.domain;

import com.rendaxx.labs.validation.ValidTimeZone;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.locationtech.jts.geom.Point;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(
        name = "retail_points",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_retail_points_name", columnNames = "name"),
                @UniqueConstraint(name = "uk_retail_points_address", columnNames = "address")
        })
public class RetailPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    private String address;

    @NotNull
    @Column(columnDefinition = "geometry(Point,4326)")
    private Point location;

    @NotNull
    @Enumerated(EnumType.STRING)
    private PointType type;

    @NotBlank
    @ValidTimeZone
    private String timezone;
}
