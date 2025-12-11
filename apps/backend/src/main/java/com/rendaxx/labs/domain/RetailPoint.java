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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jspecify.annotations.Nullable;
import org.locationtech.jts.geom.Point;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "retail_points")
public class RetailPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private @Nullable Long id;

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
