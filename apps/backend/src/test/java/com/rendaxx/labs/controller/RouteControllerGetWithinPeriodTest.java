package com.rendaxx.labs.controller;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.rendaxx.labs.controller.support.RouteTestDataFactory;
import com.rendaxx.labs.domain.Route;
import com.rendaxx.labs.repository.OrderRepository;
import com.rendaxx.labs.repository.RetailPointRepository;
import com.rendaxx.labs.repository.RouteRepository;
import com.rendaxx.labs.repository.VehicleRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
class RouteControllerGetWithinPeriodTest {

    private static final DockerImageName POSTGIS_IMAGE =
            DockerImageName.parse("postgis/postgis:16-3.4").asCompatibleSubstituteFor("postgres");

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
    private RouteRepository routeRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private RetailPointRepository retailPointRepository;

    @Autowired
    private OrderRepository orderRepository;

    private RouteTestDataFactory testDataFactory;

    @BeforeEach
    void setUp() {
        testDataFactory =
                new RouteTestDataFactory(routeRepository, vehicleRepository, retailPointRepository, orderRepository);
        testDataFactory.cleanDatabase();
    }

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
