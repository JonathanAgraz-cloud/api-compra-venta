package com.jonypacheco.marketplace.persistence.repository;

import com.jonypacheco.marketplace.persistence.domain.entity.AlertSent;
import com.jonypacheco.marketplace.persistence.domain.entity.Listing;
import com.jonypacheco.marketplace.persistence.domain.enums.GananciaClasificacion;
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

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=validate")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AlertSentRepositoryTest extends AbstractMySQLIntegrationTest {

    @Autowired
    private AlertSentRepository alertSentRepository;

    @Autowired
    private ListingRepository listingRepository;

    @Test
    void guardaYEncuentraAlertaPorListing() {
        Listing listing = listingRepository.saveAndFlush(new Listing("fb-4001", "Refrigerador",
                new BigDecimal("4000.00"), ZonaMerida.TEMOZON_NORTE, "https://facebook.com/marketplace/item/4001"));

        AlertSent alerta = new AlertSent(listing, new BigDecimal("4000.00"), new BigDecimal("5500.00"),
                new BigDecimal("1075.00"), GananciaClasificacion.MEDIA, 5);
        alertSentRepository.saveAndFlush(alerta);

        assertThat(alertSentRepository.existsByListing_Id(listing.getId())).isTrue();
        Optional<AlertSent> encontrada = alertSentRepository.findByListing_Id(listing.getId());
        assertThat(encontrada).isPresent();
        assertThat(encontrada.get().getClasificacion()).isEqualTo(GananciaClasificacion.MEDIA);
    }

    @Test
    void noPermiteDosAlertasParaElMismoListing() {
        Listing listing = listingRepository.saveAndFlush(new Listing("fb-4002", "Lavadora",
                new BigDecimal("2500.00"), ZonaMerida.FRANCISCO_DE_MONTEJO, "https://facebook.com/marketplace/item/4002"));

        alertSentRepository.saveAndFlush(new AlertSent(listing, new BigDecimal("2500.00"), new BigDecimal("3400.00"),
                new BigDecimal("560.00"), GananciaClasificacion.BAJA, 6));

        AlertSent segundaAlerta = new AlertSent(listing, new BigDecimal("2500.00"), new BigDecimal("3400.00"),
                new BigDecimal("560.00"), GananciaClasificacion.BAJA, 6);

        assertThatThrownBy(() -> alertSentRepository.saveAndFlush(segundaAlerta))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
