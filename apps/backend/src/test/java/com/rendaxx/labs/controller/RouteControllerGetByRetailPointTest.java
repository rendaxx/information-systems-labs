package com.rendaxx.labs.controller;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.rendaxx.labs.controller.support.RouteTestDataFactory;
import com.rendaxx.labs.domain.RetailPoint;
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
class RouteControllerGetByRetailPointTest {

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
