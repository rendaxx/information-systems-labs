package com.rendaxx.labs.controller.support;

import com.rendaxx.labs.repository.OrderRepository;
import com.rendaxx.labs.repository.RetailPointRepository;
import com.rendaxx.labs.repository.RouteRepository;
import com.rendaxx.labs.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest
@AutoConfigureMockMvc
public abstract class IntegrationTest {

    @DynamicPropertySource
    static void configureDatasourceProperties(DynamicPropertyRegistry registry) {
        PostgreSQLContainer<?> postgres = PostgresTestContainer.get();
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.url", postgres::getJdbcUrl);
        registry.add("spring.flyway.user", postgres::getUsername);
        registry.add("spring.flyway.password", postgres::getPassword);
    }

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected RouteRepository routeRepository;

    @Autowired
    protected VehicleRepository vehicleRepository;

    @Autowired
    protected RetailPointRepository retailPointRepository;

    @Autowired
    protected OrderRepository orderRepository;

    protected RouteTestDataFactory testDataFactory;

    @BeforeEach
    void baseSetUp() {
        this.testDataFactory =
                new RouteTestDataFactory(routeRepository, vehicleRepository, retailPointRepository, orderRepository);
        this.testDataFactory.cleanDatabase();
    }
}
