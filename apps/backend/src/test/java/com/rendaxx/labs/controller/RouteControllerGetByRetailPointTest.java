package com.rendaxx.labs.controller;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.rendaxx.labs.controller.support.IntegrationTest;
import com.rendaxx.labs.domain.RetailPoint;
import com.rendaxx.labs.domain.Route;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class RouteControllerGetByRetailPointTest extends IntegrationTest {

    @Test
    void returnsEmptyListWhenRetailPointDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/routes/retail-point/{retailPointId}", 9999L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void returnsEmptyListWhenRetailPointHasNoRoutes() throws Exception {
        RetailPoint retailPoint = testDataFactory.persistRetailPoint();

        mockMvc.perform(get("/api/routes/retail-point/{retailPointId}", retailPoint.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void returnsOnlyRoutesVisitingRetailPoint() throws Exception {
        Route routeVisitingTarget = testDataFactory.persistRoute(
                LocalDateTime.of(2025, 6, 1, 9, 0), LocalDateTime.of(2025, 6, 1, 11, 0), new BigDecimal("25.000"));
        RetailPoint targetRetailPoint =
                routeVisitingTarget.getRoutePoints().get(0).getRetailPoint();

        Route anotherRouteVisitingTarget = testDataFactory.persistRouteWithRetailPoint(
                targetRetailPoint,
                LocalDateTime.of(2025, 6, 2, 9, 0),
                LocalDateTime.of(2025, 6, 2, 11, 0),
                new BigDecimal("30.000"));

        testDataFactory.persistRoute(
                LocalDateTime.of(2025, 6, 3, 9, 0), LocalDateTime.of(2025, 6, 3, 11, 0), new BigDecimal("35.000"));

        mockMvc.perform(get("/api/routes/retail-point/{retailPointId}", targetRetailPoint.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath(
                        "$[*].id",
                        containsInAnyOrder(
                                routeVisitingTarget.getId().intValue(),
                                anotherRouteVisitingTarget.getId().intValue())));
    }

    @Test
    void returnsEachRouteOnceEvenWhenRetailPointVisitedMultipleTimes() throws Exception {
        RetailPoint retailPoint = testDataFactory.persistRetailPoint();
        Route route = testDataFactory.persistRouteWithRepeatedRetailPoint(
                retailPoint,
                LocalDateTime.of(2025, 7, 10, 8, 0),
                LocalDateTime.of(2025, 7, 10, 9, 0),
                LocalDateTime.of(2025, 7, 10, 13, 0),
                LocalDateTime.of(2025, 7, 10, 14, 0),
                new BigDecimal("45.000"));

        mockMvc.perform(get("/api/routes/retail-point/{retailPointId}", retailPoint.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(route.getId()))
                .andExpect(jsonPath("$[0].routePoints.length()").value(2));
    }
}
