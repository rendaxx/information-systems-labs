package com.rendaxx.labs.controller;

import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.rendaxx.labs.controller.support.RouteTestDataFactory;
import com.rendaxx.labs.domain.PointType;
import com.rendaxx.labs.domain.RetailPoint;
import com.rendaxx.labs.repository.OrderRepository;
import com.rendaxx.labs.repository.RetailPointRepository;
import com.rendaxx.labs.repository.RouteRepository;
import com.rendaxx.labs.repository.VehicleRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
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
class RetailPointControllerNearestTest {

    private static final DockerImageName POSTGIS_IMAGE = DockerImageName
        .parse("postgis/postgis:16-3.4")
        .asCompatibleSubstituteFor("postgres");

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(POSTGIS_IMAGE)
        .withDatabaseName("retail_points_db")
        .withUsername("retail_points_user")
        .withPassword("retail_points_pass");

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

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
    private RetailPointRepository retailPointRepository;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private OrderRepository orderRepository;

    private RouteTestDataFactory testDataFactory;

    @BeforeEach
    void setUp() {
        routeRepository.deleteAll();
        orderRepository.deleteAll();
        retailPointRepository.deleteAll();
        vehicleRepository.deleteAll();
        testDataFactory = new RouteTestDataFactory(routeRepository, vehicleRepository, retailPointRepository, orderRepository);
    }

    @Test
    void returnsBadRequestWhenLimitIsNotPositive() throws Exception {
        RetailPoint origin = persistRetailPoint(37.61, 55.75);

        mockMvc.perform(get("/api/retail-points/{id}/nearest", origin.getId()).param("limit", "0"))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Limit must be positive"));
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
                new BigDecimal("12.000")
            );
        }

        List<Long> expectedOrder = points.stream()
            .sorted(Comparator.comparingDouble(point -> distance(origin, point)))
            .map(RetailPoint::getId)
            .limit(3)
            .toList();

        mockMvc.perform(get("/api/retail-points/{id}/nearest", origin.getId()).param("limit", "3"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(3))
            .andExpect(jsonPath("$[*].id", contains(
                expectedOrder.get(0).intValue(),
                expectedOrder.get(1).intValue(),
                expectedOrder.get(2).intValue()
            )));
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
            .location(GEOMETRY_FACTORY.createPoint(new Coordinate(longitude, latitude)))
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
