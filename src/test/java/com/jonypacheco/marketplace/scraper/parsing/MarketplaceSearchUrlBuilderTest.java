package com.jonypacheco.marketplace.scraper.parsing;

import com.jonypacheco.marketplace.persistence.domain.entity.SearchConfig;
import com.jonypacheco.marketplace.persistence.domain.enums.ZonaMerida;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class MarketplaceSearchUrlBuilderTest {

    @Test
    void construyeUrlSinFiltrosDePrecioCuandoNoEstanConfigurados() {
        SearchConfig config = new SearchConfig("Prueba", "Celulares", "iphone 12", ZonaMerida.ALTABRISA);

        String url = MarketplaceSearchUrlBuilder.build(config);

        assertThat(url).isEqualTo("https://www.facebook.com/marketplace/merida/search?query=iphone+12");
    }

    @Test
    void agregaMinPriceYMaxPriceCuandoEstanConfigurados() {
        SearchConfig config = new SearchConfig("Prueba", "Celulares", "iphone", ZonaMerida.CHOLUL);
        config.setPrecioMin(new BigDecimal("1000"));
        config.setPrecioMax(new BigDecimal("8000"));

        String url = MarketplaceSearchUrlBuilder.build(config);

        assertThat(url).isEqualTo(
                "https://www.facebook.com/marketplace/merida/search?query=iphone&minPrice=1000&maxPrice=8000");
    }

    @Test
    void codificaEspaciosYAcentosEnLasPalabrasClave() {
        SearchConfig config = new SearchConfig("Prueba", "Electrodomesticos", "refrigerador señora", ZonaMerida.DZITYA);

        String url = MarketplaceSearchUrlBuilder.build(config);

        assertThat(url).contains("query=refrigerador+se");
        assertThat(url).doesNotContain(" ");
    }
}
