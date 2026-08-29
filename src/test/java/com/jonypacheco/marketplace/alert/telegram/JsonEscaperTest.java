package com.jonypacheco.marketplace.alert.telegram;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;

import static org.assertj.core.api.Assertions.assertThat;

class JsonEscaperTest {

    @ParameterizedTest
    @CsvSource({
            "'iPhone 12 128GB', 'iPhone 12 128GB'",
            "'Bicicleta Montaña', 'Bicicleta Montaña'",
            "'Sin cambios aqui', 'Sin cambios aqui'"
    })
    void dejaTextoNormalSinCambios(String entrada, String esperado) {
        assertThat(JsonEscaper.escape(entrada)).isEqualTo(esperado);
    }

    @Test
    void escapaComillasDobles() {
        assertThat(JsonEscaper.escape("iPhone \"como nuevo\"")).isEqualTo("iPhone \\\"como nuevo\\\"");
    }

    @Test
    void escapaBackslash() {
        assertThat(JsonEscaper.escape("C:\\ruta")).isEqualTo("C:\\\\ruta");
    }

    @Test
    void escapaSaltosDeLineaYTab() {
        assertThat(JsonEscaper.escape("linea1\nlinea2\ttab")).isEqualTo("linea1\\nlinea2\\ttab");
    }

    @Test
    void escapaCaracteresDeControl() {
        assertThat(JsonEscaper.escape("a\u0001b")).isEqualTo("a\\u0001b");
    }

    @ParameterizedTest
    @NullSource
    void devuelveVacioParaNull(String entrada) {
        assertThat(JsonEscaper.escape(entrada)).isEmpty();
    }
}
