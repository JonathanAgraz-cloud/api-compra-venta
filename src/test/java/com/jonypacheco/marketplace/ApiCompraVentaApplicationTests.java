package com.jonypacheco.marketplace;

import com.jonypacheco.marketplace.persistence.repository.support.AbstractMySQLIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Levanta el contexto completo de Spring contra MySQL real. Si hay un error
 * de mapeo JPA, una migracion Flyway rota, o una entidad que no coincide con
 * el schema (ddl-auto: validate), este test falla.
 */
@SpringBootTest
class ApiCompraVentaApplicationTests extends AbstractMySQLIntegrationTest {

    @Test
    void contextLoads() {
        // Si el contexto levanta sin excepciones, la validacion paso.
    }
}
