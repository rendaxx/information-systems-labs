package com.rendaxx.labs.controller.support;

import com.rendaxx.labs.repository.OrderRepository;
import com.rendaxx.labs.repository.RetailPointRepository;
import com.rendaxx.labs.repository.RouteRepository;
import com.rendaxx.labs.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(initializers = PostgresInitializer.class)
public abstract class IntegrationTest {

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
