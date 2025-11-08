package com.rendaxx.labs.controller.support;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public final class PostgresTestContainer {

    private static final DockerImageName POSTGIS_IMAGE =
            DockerImageName.parse("postgis/postgis:16-3.4").asCompatibleSubstituteFor("postgres");

    private static final PostgreSQLContainer<?> INSTANCE = new PostgreSQLContainer<>(POSTGIS_IMAGE)
            .withDatabaseName("labs_test_db")
            .withUsername("labs_user")
            .withPassword("labs_pass");

    static {
        INSTANCE.start();
    }

    private PostgresTestContainer() {}

    public static PostgreSQLContainer<?> get() {
        return INSTANCE;
    }
}
