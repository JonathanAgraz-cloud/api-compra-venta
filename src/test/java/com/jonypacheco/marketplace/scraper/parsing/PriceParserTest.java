package com.jonypacheco.marketplace.scraper.parsing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class PriceParserTest {

    @ParameterizedTest
    @CsvSource({
            "'MX$1,500', 1500",
            "'MX$1,500.50', 1500.50",
            "'$999', 999",
            "'$1,234,567', 1234567"
    })
    void parseaFormatosValidosDePrecio(String raw, String esperado) {
        Optional<BigDecimal> resultado = PriceParser.parse(raw);

        assertThat(resultado).isPresent();
        assertThat(resultado.get()).isEqualByComparingTo(new BigDecimal(esperado));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Gratis", "Free", "   "})
    @NullAndEmptySource
    void devuelveVacioParaTextoSinNumero(String raw) {
        assertThat(PriceParser.parse(raw)).isEmpty();
    }

    @Test
    void ignoraEspacioNoDivisibleAlrededorDelNumero() {
        String conNbsp = "MX$\u00A01,500";

        Optional<BigDecimal> resultado = PriceParser.parse(conNbsp);

        assertThat(resultado).contains(new BigDecimal("1500"));
    }
}
