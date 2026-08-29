package com.jonypacheco.marketplace.analysis.pricing;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class ProductNormalizerTest {

    @ParameterizedTest
    @CsvSource({
            "'iPhone 12 128GB', 'iphone 12 128gb'",
            "'Refrigeradores', 'refrigeradores'",
            "'Bicicleta Montaña', 'bicicleta montana'",
            "'Television', 'television'"
    })
    void normalizaAMinusculasYSinAcentos(String categoria, String esperado) {
        assertThat(ProductNormalizer.normalize(categoria)).isEqualTo(esperado);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "  iPhone   12  ",
            "iPhone 12",
            "\tiPhone 12\n"
    })
    void colapsaEspaciosMultiplesYRecortaExtremos(String categoria) {
        assertThat(ProductNormalizer.normalize(categoria)).isEqualTo("iphone 12");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void devuelveNullParaCategoriaVaciaONula(String categoria) {
        assertThat(ProductNormalizer.normalize(categoria)).isNull();
    }
}
