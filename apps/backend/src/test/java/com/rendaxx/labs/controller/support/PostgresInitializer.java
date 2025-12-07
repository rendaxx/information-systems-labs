package com.rendaxx.labs.controller.support;

import org.jetbrains.annotations.NotNull;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public class PostgresInitializer implements ApplicationContextInitializer<@NotNull ConfigurableApplicationContext> {

    private final DockerImageName image =
            DockerImageName.parse("postgis/postgis:16-3.4").asCompatibleSubstituteFor("postgres");

    private final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(image)
            .withDatabaseName("labs_test_db")
            .withUsername("labs_user")
            .withPassword("labs_pass");

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        if (!postgres.isRunning()) {
            postgres.start();
        }

        TestPropertyValues.of(
                        "spring.datasource.url=" + postgres.getJdbcUrl(),
                        "spring.datasource.username=" + postgres.getUsername(),
                        "spring.datasource.password=" + postgres.getPassword(),
                        "spring.flyway.url=" + postgres.getJdbcUrl(),
                        "spring.flyway.user=" + postgres.getUsername(),
                        "spring.flyway.password=" + postgres.getPassword())
                .applyTo(applicationContext.getEnvironment());

        applicationContext.addApplicationListener((ContextClosedEvent event) -> {
            if (postgres.isRunning()) {
                postgres.stop();
            }
        });
    }
}
