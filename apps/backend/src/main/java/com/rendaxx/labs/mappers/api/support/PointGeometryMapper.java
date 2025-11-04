package com.rendaxx.labs.mappers.api.support;

import com.rendaxx.labs.api.v1.model.PointApiDto;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Component;

@Component
public class PointGeometryMapper {

    private static final int SRID = 4326;
    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), SRID);

    public Point toPoint(PointApiDto dto) {
        if (dto == null) {
            return null;
        }
        Double longitude = dto.getLongitude();
        Double latitude = dto.getLatitude();
        if (longitude == null || latitude == null) {
            return null;
        }
        Point point = GEOMETRY_FACTORY.createPoint(new Coordinate(longitude, latitude));
        point.setSRID(SRID);
        return point;
    }
}
