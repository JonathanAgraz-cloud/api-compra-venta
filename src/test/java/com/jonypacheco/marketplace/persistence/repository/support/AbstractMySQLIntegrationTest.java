package com.jonypacheco.marketplace.persistence.repository.support;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;

/**
 * Base para tests que necesitan un MySQL real (no H2), compartido entre
 * TODAS las clases de test que la extienden (patron "singleton container"
 * de Testcontainers).
 *
 * <p>Deliberadamente NO se usa {@code @Container}/{@code @ServiceConnection}:
 * esas anotaciones hacen que JUnit detenga el contenedor al terminar cada
 * clase de test, y como el campo estatico se hereda (una sola instancia para
 * todas las subclases), la primera clase en terminar lo mataria para las
 * demas. En su lugar se arranca una sola vez de forma manual y se registran
 * las propiedades de conexion via {@code @DynamicPropertySource}; el
 * contenedor lo limpia el Ryuk resource reaper de Testcontainers al terminar
 * la JVM.
 */
public abstract class AbstractMySQLIntegrationTest {

    static final MySQLContainer<?> MYSQL_CONTAINER = new MySQLContainer<>("mysql:8.4");

    static {
        MYSQL_CONTAINER.start();
    }

    @DynamicPropertySource
    static void mysqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", MYSQL_CONTAINER::getPassword);
    }
}
