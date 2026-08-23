package com.jonypacheco.marketplace.persistence.repository;

import com.jonypacheco.marketplace.persistence.domain.entity.SearchConfig;
import com.jonypacheco.marketplace.persistence.domain.enums.ZonaMerida;
import com.jonypacheco.marketplace.persistence.repository.support.AbstractMySQLIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=validate")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SearchConfigRepositoryTest extends AbstractMySQLIntegrationTest {

    @Autowired
    private SearchConfigRepository searchConfigRepository;

    @Test
    void encuentraSoloConfigsActivas() {
        SearchConfig activa = new SearchConfig("iPhones Altabrisa", "Celulares", "iphone", ZonaMerida.ALTABRISA);
        SearchConfig inactiva = new SearchConfig("Bicicletas viejas", "Deportes", "bicicleta", ZonaMerida.CHOLUL);
        inactiva.setActivo(false);

        searchConfigRepository.saveAndFlush(activa);
        searchConfigRepository.saveAndFlush(inactiva);

        List<SearchConfig> activas = searchConfigRepository.findByActivoTrue();

        assertThat(activas).extracting(SearchConfig::getNombre).containsExactly("iPhones Altabrisa");
    }

    @Test
    void encuentraConfigsActivasPorZona() {
        searchConfigRepository.saveAndFlush(
                new SearchConfig("Laptops Dzitya", "Computadoras", "laptop", ZonaMerida.DZITYA));
        searchConfigRepository.saveAndFlush(
                new SearchConfig("Laptops general", "Computadoras", "laptop", ZonaMerida.OTRA_ZONA));

        List<SearchConfig> enDzitya = searchConfigRepository.findByZonaAndActivoTrue(ZonaMerida.DZITYA);

        assertThat(enDzitya).hasSize(1);
        assertThat(enDzitya.get(0).getZona()).isEqualTo(ZonaMerida.DZITYA);
    }
}
