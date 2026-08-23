package com.jonypacheco.marketplace.persistence.repository;

import com.jonypacheco.marketplace.persistence.domain.entity.Listing;
import com.jonypacheco.marketplace.persistence.domain.enums.ZonaMerida;
import com.jonypacheco.marketplace.persistence.repository.support.AbstractMySQLIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// Replace.NONE: queremos validar el schema MySQL real (via Testcontainers +
// Flyway), no un H2 embebido que @DataJpaTest usaria por defecto.
@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=validate")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ListingRepositoryTest extends AbstractMySQLIntegrationTest {

    @Autowired
    private ListingRepository listingRepository;

    @Test
    void guardaYEncuentraPorFacebookId() {
        Listing listing = new Listing("fb-1001", "iPhone 12 128GB", new BigDecimal("6500.00"),
                ZonaMerida.ALTABRISA, "https://facebook.com/marketplace/item/1001");

        listingRepository.saveAndFlush(listing);

        assertThat(listingRepository.existsByFacebookId("fb-1001")).isTrue();
        Optional<Listing> encontrado = listingRepository.findByFacebookId("fb-1001");
        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getTitulo()).isEqualTo("iPhone 12 128GB");
    }

    @Test
    void noPermiteFacebookIdDuplicado() {
        listingRepository.saveAndFlush(new Listing("fb-2001", "Bicicleta de montana", new BigDecimal("3000.00"),
                ZonaMerida.CHOLUL, "https://facebook.com/marketplace/item/2001"));

        Listing duplicado = new Listing("fb-2001", "Otro titulo", new BigDecimal("100.00"),
                ZonaMerida.DZITYA, "https://facebook.com/marketplace/item/2001-otro");

        assertThatThrownBy(() -> listingRepository.saveAndFlush(duplicado))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
