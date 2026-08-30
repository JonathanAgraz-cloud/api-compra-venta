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
import java.util.List;
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

    @Test
    void findAllByOrderByGananciaEstimadaDesc_regresaOrdenadoDeMayorAMenorGananciaConListingCargado() {
        Listing listingBaja = listingRepository.saveAndFlush(new Listing("fb-4003", "Mesa de centro",
                new BigDecimal("500.00"), ZonaMerida.CHOLUL, "https://facebook.com/marketplace/item/4003"));
        Listing listingAlta = listingRepository.saveAndFlush(new Listing("fb-4004", "Refrigerador industrial",
                new BigDecimal("8000.00"), ZonaMerida.ALTABRISA, "https://facebook.com/marketplace/item/4004"));
        Listing listingMedia = listingRepository.saveAndFlush(new Listing("fb-4005", "Lavadora doble carga",
                new BigDecimal("3000.00"), ZonaMerida.DZITYA, "https://facebook.com/marketplace/item/4005"));

        alertSentRepository.saveAndFlush(new AlertSent(listingBaja, new BigDecimal("500.00"), new BigDecimal("1050.00"),
                new BigDecimal("500.00"), GananciaClasificacion.BAJA, 5));
        alertSentRepository.saveAndFlush(new AlertSent(listingAlta, new BigDecimal("8000.00"), new BigDecimal("11000.00"),
                new BigDecimal("2500.00"), GananciaClasificacion.ALTA, 7));
        alertSentRepository.saveAndFlush(new AlertSent(listingMedia, new BigDecimal("3000.00"), new BigDecimal("4700.00"),
                new BigDecimal("1200.00"), GananciaClasificacion.MEDIA, 6));

        List<AlertSent> ordenadas = alertSentRepository.findAllByOrderByGananciaEstimadaDesc();

        assertThat(ordenadas).hasSize(3);
        assertThat(ordenadas).extracting(a -> a.getGananciaEstimada().stripTrailingZeros())
                .containsExactly(
                        new BigDecimal("2500.00").stripTrailingZeros(),
                        new BigDecimal("1200.00").stripTrailingZeros(),
                        new BigDecimal("500.00").stripTrailingZeros());
        // El listing debe venir cargado (EntityGraph) sin necesitar otra consulta.
        assertThat(ordenadas.get(0).getListing().getTitulo()).isEqualTo("Refrigerador industrial");
    }
}
