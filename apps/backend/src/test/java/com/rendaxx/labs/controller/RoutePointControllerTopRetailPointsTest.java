package com.rendaxx.labs.controller;

import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.rendaxx.labs.controller.support.IntegrationTest;
import com.rendaxx.labs.domain.RetailPoint;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class RoutePointControllerTopRetailPointsTest extends IntegrationTest {

    @Test
    void returnsEmptyListWhenNoRoutePointsExist() throws Exception {
        mockMvc.perform(get("/api/route-points/top-retail-points").param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void returnsBadRequestWhenLimitIsNotPositive() throws Exception {
        mockMvc.perform(get("/api/route-points/top-retail-points").param("limit", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returnsTopRetailPointsLimitedByRequestedSize() throws Exception {
        RetailPoint mostVisited = testDataFactory.persistRetailPoint();
        RetailPoint secondMostVisited = testDataFactory.persistRetailPoint();
        RetailPoint other = testDataFactory.persistRetailPoint();

        createVisits(mostVisited, 3, LocalDateTime.of(2025, 8, 1, 8, 0));
        createVisits(secondMostVisited, 2, LocalDateTime.of(2025, 8, 10, 8, 0));
        createVisits(other, 1, LocalDateTime.of(2025, 8, 20, 8, 0));

        mockMvc.perform(get("/api/route-points/top-retail-points").param("limit", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(mostVisited.getId()))
                .andExpect(jsonPath("$[1].id").value(secondMostVisited.getId()));
    }

    @Test
    void ordersRetailPointsByIdWhenVisitCountsAreEqual() throws Exception {
        RetailPoint first = testDataFactory.persistRetailPoint();
        RetailPoint second = testDataFactory.persistRetailPoint();

        createVisits(first, 1, LocalDateTime.of(2025, 9, 1, 9, 0));
        createVisits(second, 1, LocalDateTime.of(2025, 9, 2, 9, 0));

        mockMvc.perform(get("/api/route-points/top-retail-points").param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                        "$[*].id",
                        contains(first.getId().intValue(), second.getId().intValue())));
    }

    private void createVisits(RetailPoint retailPoint, int count, LocalDateTime startTime) {
        for (int i = 0; i < count; i++) {
            LocalDateTime plannedStart = startTime.plusDays(i);
            LocalDateTime plannedEnd = plannedStart.plusHours(1);
            testDataFactory.persistRouteWithRetailPoint(
                    retailPoint, plannedStart, plannedEnd, new BigDecimal("10.000"));
        }
    }
}
