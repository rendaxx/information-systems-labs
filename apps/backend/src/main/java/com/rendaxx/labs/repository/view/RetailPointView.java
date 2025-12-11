package com.rendaxx.labs.repository.view;

import com.rendaxx.labs.domain.PointType;
import org.locationtech.jts.geom.Point;

public interface RetailPointView {
    Long getId();

    String getName();

    String getAddress();

    Point getLocation();

    PointType getType();

    String getTimezone();
}
