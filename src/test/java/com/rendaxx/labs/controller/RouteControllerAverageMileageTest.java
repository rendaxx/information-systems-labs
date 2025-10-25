package com.rendaxx.labs.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class RouteControllerAverageMileageTest {

    private static final LocalDateTime PLANNED_START = LocalDateTime.of(2025, 1, 1, 0, 0);
    private static final LocalDateTime PLANNED_END = LocalDateTime.of(2025, 1, 1, 2, 0);

    private static final DockerImageName POSTGIS_IMAGE = DockerImageName.parse("postgis/postgis:16-3.4")
        .asCompatibleSubstituteFor("postgres");

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(POSTGIS_IMAGE)
        .withDatabaseName("routes_db")
        .withUsername("routes_user")
        .withPassword("routes_pass");

    @DynamicPropertySource
    static void configureDatasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.flyway.url", POSTGRES::getJdbcUrl);
        registry.add("spring.flyway.user", POSTGRES::getUsername);
        registry.add("spring.flyway.password", POSTGRES::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.execute("TRUNCATE TABLE route_point_orders, route_points, routes RESTART IDENTITY CASCADE");
    }

    @Test
    void averageMileageReturnsZeroWhenNoRoutesExist() throws Exception {
        mockMvc.perform(get("/api/routes/average-mileage"))
            .andExpect(status().isOk())
            .andExpect(content().string("0.000"));
    }

    @Test
    void averageMileageReturnsExactValueForSingleRoute() throws Exception {
        insertRoute(new BigDecimal("123.456"));

        mockMvc.perform(get("/api/routes/average-mileage"))
            .andExpect(status().isOk())
            .andExpect(content().string("123.456"));
    }

    @Test
    void averageMileageRoundsHalfUpToThreeDecimalPlaces() throws Exception {
        insertRoute(new BigDecimal("10.120"));
        insertRoute(new BigDecimal("10.121"));

        mockMvc.perform(get("/api/routes/average-mileage"))
            .andExpect(status().isOk())
            .andExpect(content().string("10.121"));
    }

    private void insertRoute(BigDecimal mileage) {
        jdbcTemplate.update(
            "INSERT INTO routes (vehicle_id, planned_start_time, planned_end_time, mileage_in_km) VALUES (?, ?, ?, ?)",
            ps -> {
                ps.setNull(1, Types.BIGINT);
                ps.setTimestamp(2, Timestamp.valueOf(PLANNED_START));
                ps.setTimestamp(3, Timestamp.valueOf(PLANNED_END));
                ps.setBigDecimal(4, mileage);
            }
        );
    }
}
