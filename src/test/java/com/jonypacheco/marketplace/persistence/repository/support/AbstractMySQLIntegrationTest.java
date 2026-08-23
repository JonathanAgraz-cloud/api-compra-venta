package com.jonypacheco.marketplace.persistence.repository.support;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base para tests que necesitan un MySQL real (no H2). El contenedor se
 * comparte entre las subclases porque es {@code static}; Spring Boot 3.1+
 * detecta {@code @ServiceConnection} y autoconfigura el datasource contra
 * el contenedor, incluyendo que Flyway corra las migraciones reales antes
 * de que arranque el contexto.
 */
@Testcontainers
public abstract class AbstractMySQLIntegrationTest {

    @Container
    @ServiceConnection
    static final MySQLContainer<?> MYSQL_CONTAINER = new MySQLContainer<>("mysql:8.4");
}
