package com.jonypacheco.marketplace.persistence.repository;

import com.jonypacheco.marketplace.persistence.domain.entity.ComparableListing;
import com.jonypacheco.marketplace.persistence.domain.entity.Listing;
import com.jonypacheco.marketplace.persistence.domain.enums.ZonaMerida;
import com.jonypacheco.marketplace.persistence.repository.support.AbstractMySQLIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=validate")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ComparableListingRepositoryTest extends AbstractMySQLIntegrationTest {

    private static final String PRODUCTO = "iPhone 12 128GB";

    @Autowired
    private ComparableListingRepository comparableListingRepository;

    @Autowired
    private ListingRepository listingRepository;

    @Test
    void guardaComparableConYSinListingOrigen() {
        Listing origen = listingRepository.saveAndFlush(new Listing("fb-3001", PRODUCTO, new BigDecimal("7000.00"),
                ZonaMerida.YUCATAN_COUNTRY_CLUB, "https://facebook.com/marketplace/item/3001"));

        ComparableListing conOrigen = new ComparableListing(PRODUCTO, "Celulares", new BigDecimal("7200.00"));
        conOrigen.setSourceListing(origen);
        comparableListingRepository.saveAndFlush(conOrigen);

        ComparableListing sinOrigen = new ComparableListing(PRODUCTO, "Celulares", new BigDecimal("7100.00"));
        comparableListingRepository.saveAndFlush(sinOrigen);

        List<ComparableListing> resultado = comparableListingRepository.findBySourceListing_Id(origen.getId());
        assertThat(resultado).hasSize(1);
        assertThat(sinOrigen.getSourceListing()).isNull();
    }

    @Test
    void cuentaComparablesConfiablesPorProductoNormalizado() {
        for (int i = 0; i < 5; i++) {
            ComparableListing comparable = new ComparableListing(PRODUCTO, "Celulares",
                    new BigDecimal("7000.00").add(BigDecimal.valueOf(i * 50)));
            comparableListingRepository.saveAndFlush(comparable);
        }
        ComparableListing noConfiable = new ComparableListing(PRODUCTO, "Celulares", new BigDecimal("1.00"));
        noConfiable.setConfiable(false);
        comparableListingRepository.saveAndFlush(noConfiable);

        long confiables = comparableListingRepository.countByProductoNormalizadoAndConfiableTrue(PRODUCTO);

        assertThat(confiables).isEqualTo(5);
    }
}
