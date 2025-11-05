package com.rendaxx.labs.domain;

import com.rendaxx.labs.validation.ValidTimeZone;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
