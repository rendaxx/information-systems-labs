package com.rendaxx.labs.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.rendaxx.labs.controller.support.RouteTestDataFactory;
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
class RouteControllerAverageMileageTest {

    private static final DockerImageName POSTGIS_IMAGE = DockerImageName
        .parse("postgis/postgis:16-3.4")
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
        testDataFactory = new RouteTestDataFactory(routeRepository, vehicleRepository, retailPointRepository, orderRepository);
        testDataFactory.cleanDatabase();
    }

    @Test
    void averageMileageReturnsZeroWhenNoRoutesExist() throws Exception {
        mockMvc.perform(get("/api/routes/average-mileage"))
            .andExpect(status().isOk())
            .andExpect(content().string("0.000"));
    }

    @Test
    void averageMileageReturnsExactValueForSingleRoute() throws Exception {
        testDataFactory.persistRoute(LocalDateTime.of(2025, 1, 1, 8, 0), LocalDateTime.of(2025, 1, 1, 10, 0), new BigDecimal("123.456"));

        mockMvc.perform(get("/api/routes/average-mileage"))
            .andExpect(status().isOk())
            .andExpect(content().string("123.456"));
    }

    @Test
    void averageMileageRoundsHalfUpToThreeDecimalPlaces() throws Exception {
        testDataFactory.persistRoute(LocalDateTime.of(2025, 2, 1, 9, 0), LocalDateTime.of(2025, 2, 1, 11, 0), new BigDecimal("10.120"));
        testDataFactory.persistRoute(LocalDateTime.of(2025, 2, 2, 9, 0), LocalDateTime.of(2025, 2, 2, 11, 0), new BigDecimal("10.121"));

        mockMvc.perform(get("/api/routes/average-mileage"))
            .andExpect(status().isOk())
            .andExpect(content().string("10.121"));
    }

}
