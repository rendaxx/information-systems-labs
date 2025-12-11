package com.rendaxx.labs.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.rendaxx.labs.controller.support.IntegrationTest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class RouteControllerAverageMileageTest extends IntegrationTest {

    @Test
    void averageMileageReturnsZeroWhenNoRoutesExist() throws Exception {
        mockMvc.perform(get("/api/routes/average-mileage"))
                .andExpect(status().isOk())
                .andExpect(content().string("0.0"));
    }

    @Test
    void averageMileageReturnsExactValueForSingleRoute() throws Exception {
        testDataFactory.persistRoute(
                LocalDateTime.of(2025, 1, 1, 8, 0), LocalDateTime.of(2025, 1, 1, 10, 0), new BigDecimal("123.456"));

        mockMvc.perform(get("/api/routes/average-mileage"))
                .andExpect(status().isOk())
                .andExpect(content().string("123.456"));
    }

    @Test
    void averageMileageRoundsHalfUpToThreeDecimalPlaces() throws Exception {
        testDataFactory.persistRoute(
                LocalDateTime.of(2025, 2, 1, 9, 0), LocalDateTime.of(2025, 2, 1, 11, 0), new BigDecimal("10.120"));
        testDataFactory.persistRoute(
                LocalDateTime.of(2025, 2, 2, 9, 0), LocalDateTime.of(2025, 2, 2, 11, 0), new BigDecimal("10.121"));

        mockMvc.perform(get("/api/routes/average-mileage"))
                .andExpect(status().isOk())
                .andExpect(content().string("10.121"));
    }
}
