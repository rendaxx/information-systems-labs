package com.rendaxx.labs.controller;

import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.rendaxx.labs.controller.support.IntegrationTest;
import com.rendaxx.labs.domain.PointType;
import com.rendaxx.labs.domain.RetailPoint;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.Test;

class RetailPointControllerNearestTest extends IntegrationTest {

    @Test
    void returnsBadRequestWhenLimitIsNotPositive() throws Exception {
        RetailPoint origin = persistRetailPoint(37.61, 55.75);

        mockMvc.perform(get("/api/retail-points/{id}/nearest", origin.getId()).param("limit", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returnsNotFoundWhenOriginRetailPointDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/retail-points/{id}/nearest", 9999L).param("limit", "3"))
                .andExpect(status().isNotFound());
    }

    @Test
    void returnsEmptyListWhenNoOtherRetailPoints() throws Exception {
        RetailPoint origin = persistRetailPoint(37.61, 55.75);

        mockMvc.perform(get("/api/retail-points/{id}/nearest", origin.getId()).param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void returnsNearestRetailPointsOrderedByDistance() throws Exception {
        RetailPoint origin = persistRetailPoint(37.61, 55.75);
        List<RetailPoint> points = new ArrayList<>();
        points.add(persistRetailPoint(37.6101, 55.7501));
        points.add(persistRetailPoint(37.611, 55.751));
        points.add(persistRetailPoint(37.63, 55.77));
        points.add(persistRetailPoint(37.9, 55.9));

        for (RetailPoint point : points) {
            testDataFactory.persistRouteWithRetailPoint(
                    point,
                    LocalDateTime.of(2025, 10, 1, 8, 0),
                    LocalDateTime.of(2025, 10, 1, 10, 0),
                    new BigDecimal("12.000"));
        }

        List<Long> expectedOrder = points.stream()
                .sorted(Comparator.comparingDouble(point -> distance(origin, point)))
                .map(RetailPoint::getId)
                .limit(3)
                .toList();

        mockMvc.perform(get("/api/retail-points/{id}/nearest", origin.getId()).param("limit", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath(
                        "$[*].id",
                        contains(
                                expectedOrder.get(0).intValue(),
                                expectedOrder.get(1).intValue(),
                                expectedOrder.get(2).intValue())));
    }

    @Test
    void breaksTiesByRetailPointIdWhenDistancesAreEqual() throws Exception {
        RetailPoint origin = persistRetailPoint(40.0, 60.0);
        RetailPoint first = persistRetailPoint(40.0, 60.01);
        RetailPoint second = persistRetailPoint(40.0, 59.99);

        int smallerId = Math.min(first.getId().intValue(), second.getId().intValue());
        int largerId = Math.max(first.getId().intValue(), second.getId().intValue());

        mockMvc.perform(get("/api/retail-points/{id}/nearest", origin.getId()).param("limit", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[*].id", contains(smallerId, largerId)));
    }

    private RetailPoint persistRetailPoint(double longitude, double latitude) {
        RetailPoint point = RetailPoint.builder()
                .name("Retail-" + longitude + "-" + latitude)
                .address("Address-" + longitude + "-" + latitude)
                .location(testDataFactory.createPoint(longitude, latitude))
                .type(PointType.SHOP)
                .timezone("UTC")
                .build();
        return retailPointRepository.save(point);
    }

    private double distance(RetailPoint origin, RetailPoint target) {
        double dx = origin.getLocation().getX() - target.getLocation().getX();
        double dy = origin.getLocation().getY() - target.getLocation().getY();
        return Math.sqrt(dx * dx + dy * dy);
    }
}
