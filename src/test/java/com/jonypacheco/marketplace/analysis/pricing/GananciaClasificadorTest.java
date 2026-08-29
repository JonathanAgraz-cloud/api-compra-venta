package com.jonypacheco.marketplace.analysis.pricing;

import com.jonypacheco.marketplace.persistence.domain.enums.GananciaClasificacion;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class GananciaClasificadorTest {

    @ParameterizedTest
    @CsvSource({
            "499.99",
            "0",
            "-100"
    })
    void descartaGananciasMenoresAlMinimoDeAlerta(String ganancia) {
        assertThat(GananciaClasificador.clasificar(new BigDecimal(ganancia))).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
            "500, BAJA",
            "999.99, BAJA",
            "1000, MEDIA",
            "1999.99, MEDIA",
            "2000, ALTA",
            "5000, ALTA"
    })
    void clasificaPorRangoDeGanancia(String ganancia, GananciaClasificacion esperada) {
        Optional<GananciaClasificacion> resultado = GananciaClasificador.clasificar(new BigDecimal(ganancia));

        assertThat(resultado).contains(esperada);
    }
}
