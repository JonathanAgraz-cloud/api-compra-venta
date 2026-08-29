package com.jonypacheco.marketplace.scraper.parsing;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import static org.assertj.core.api.Assertions.assertThat;

class FacebookUrlResolverTest {

    @ParameterizedTest
    @CsvSource({
            "/marketplace/item/123/?ref=search, https://www.facebook.com/marketplace/item/123/?ref=search",
            "marketplace/item/123/, https://www.facebook.com/marketplace/item/123/"
    })
    void anteponeElOrigenAUnaRutaRelativa(String href, String esperado) {
        assertThat(FacebookUrlResolver.resolve(href)).isEqualTo(esperado);
    }

    @ParameterizedTest
    @CsvSource({
            "https://www.facebook.com/marketplace/item/123/, https://www.facebook.com/marketplace/item/123/",
            "http://www.facebook.com/marketplace/item/123/, http://www.facebook.com/marketplace/item/123/"
    })
    void dejaUnaUrlYaAbsolutaSinCambios(String href, String esperado) {
        assertThat(FacebookUrlResolver.resolve(href)).isEqualTo(esperado);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void esSeguroConNuloOVacio(String href) {
        assertThat(FacebookUrlResolver.resolve(href)).isEqualTo(href);
    }
}
