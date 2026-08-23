package com.jonypacheco.marketplace.scraper.parsing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ListingIdExtractorTest {

    @ParameterizedTest
    @CsvSource({
            "https://www.facebook.com/marketplace/item/123456789/, 123456789",
            "https://www.facebook.com/marketplace/item/987654321, 987654321",
            "https://www.facebook.com/marketplace/item/555000111/?ref=search&referral_code=null, 555000111"
    })
    void extraeElIdDeUrlsValidas(String url, String idEsperado) {
        Optional<String> resultado = ListingIdExtractor.extract(url);

        assertThat(resultado).contains(idEsperado);
    }

    @ParameterizedTest
    @CsvSource({
            "https://www.facebook.com/marketplace/merida/search?query=iphone",
            "https://www.facebook.com/some/other/path",
            "not-a-url"
    })
    void devuelveVacioParaUrlsSinIdDeAnuncio(String url) {
        assertThat(ListingIdExtractor.extract(url)).isEmpty();
    }

    @Test
    void devuelveVacioParaUrlNula() {
        assertThat(ListingIdExtractor.extract(null)).isEmpty();
    }
}
