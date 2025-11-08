package com.rendaxx.labs.controller;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.rendaxx.labs.controller.support.IntegrationTest;
import com.rendaxx.labs.domain.Route;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class RouteControllerGetWithinPeriodTest extends IntegrationTest {

    @Test
    void returnsEmptyListWhenNoRoutesMatch() throws Exception {
        mockMvc.perform(get("/api/routes/within-period")
                        .param(
                                "periodStart",
                                LocalDateTime.of(2025, 1, 2, 10, 0).toString())
                        .param("periodEnd", LocalDateTime.of(2025, 1, 2, 12, 0).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void returnsOnlyRoutesFullyWithinPeriod() throws Exception {
        LocalDateTime periodStart = LocalDateTime.of(2025, 1, 2, 10, 0);
        LocalDateTime periodEnd = LocalDateTime.of(2025, 1, 2, 12, 0);

        Route included = testDataFactory.persistRoute(
                LocalDateTime.of(2025, 1, 2, 10, 30), LocalDateTime.of(2025, 1, 2, 11, 0), new BigDecimal("50.000"));
        testDataFactory.persistRoute(periodStart.minusHours(2), periodStart.minusHours(1), new BigDecimal("10.000"));
        testDataFactory.persistRoute(
                LocalDateTime.of(2025, 1, 2, 8, 0), LocalDateTime.of(2025, 1, 2, 10, 30), new BigDecimal("20.000"));
        testDataFactory.persistRoute(
                LocalDateTime.of(2025, 1, 2, 11, 30), periodEnd.plusMinutes(15), new BigDecimal("70.000"));

        mockMvc.perform(get("/api/routes/within-period")
                        .param("periodStart", periodStart.toString())
                        .param("periodEnd", periodEnd.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(included.getId()));
    }

    @Test
    void includesRoutesOnExactBoundary() throws Exception {
        LocalDateTime periodStart = LocalDateTime.of(2025, 3, 10, 9, 0);
        LocalDateTime periodEnd = LocalDateTime.of(2025, 3, 10, 15, 0);

        Route boundaryRoute = testDataFactory.persistRoute(periodStart, periodEnd, new BigDecimal("75.000"));
        Route innerRoute = testDataFactory.persistRoute(
                periodStart.plusMinutes(30), periodEnd.minusMinutes(30), new BigDecimal("40.000"));

        mockMvc.perform(get("/api/routes/within-period")
                        .param("periodStart", periodStart.toString())
                        .param("periodEnd", periodEnd.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath(
                        "$[*].id",
                        containsInAnyOrder(
                                boundaryRoute.getId().intValue(),
                                innerRoute.getId().intValue())));
    }

    @Test
    void returnsBadRequestWhenPeriodStartAfterPeriodEnd() throws Exception {
        LocalDateTime periodEnd = LocalDateTime.of(2025, 1, 5, 10, 0);

        mockMvc.perform(get("/api/routes/within-period")
                        .param("periodStart", periodEnd.plusHours(1).toString())
                        .param("periodEnd", periodEnd.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Period start must not be after period end"));
    }
}
