package com.rendaxx.labs.controller;

import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.rendaxx.labs.controller.support.RouteTestDataFactory;
import com.rendaxx.labs.domain.RetailPoint;
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
class RoutePointControllerTopRetailPointsTest {

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
    void returnsEmptyListWhenNoRoutePointsExist() throws Exception {
        mockMvc.perform(get("/api/route-points/top-retail-points").param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void returnsBadRequestWhenLimitIsNotPositive() throws Exception {
        mockMvc.perform(get("/api/route-points/top-retail-points").param("limit", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Limit must be positive"));
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
