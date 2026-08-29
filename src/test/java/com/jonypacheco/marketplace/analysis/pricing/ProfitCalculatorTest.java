package com.jonypacheco.marketplace.analysis.pricing;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProfitCalculatorTest {

    @Test
    void usaElComparableMasBaratoSinImportarElOrdenDeLaLista() {
        BigDecimal precioCompra = new BigDecimal("1000");
        List<BigDecimal> comparables = List.of(
                new BigDecimal("2500"),
                new BigDecimal("2000"),
                new BigDecimal("3000"),
                new BigDecimal("2200"),
                new BigDecimal("2100"));

        ProfitCalculation calculo = ProfitCalculator.calculate(precioCompra, comparables);

        // reventa = 2000 * 0.95 = 1900.00 ; costos = 1900 * 0.15 = 285.00
        // ganancia = 1900 - 1000 - 285 = 615.00
        assertThat(calculo.precioReventaEstimado()).isEqualByComparingTo(new BigDecimal("1900.00"));
        assertThat(calculo.gananciaEstimada()).isEqualByComparingTo(new BigDecimal("615.00"));
    }

    @Test
    void redondeaHalfUpEnCadaPasoIntermedio() {
        // comparable mas barato 999 -> reventa = 999 * 0.95 = 949.05
        // costos = 949.05 * 0.15 = 142.3575 -> redondeado 142.36
        // ganancia = 949.05 - 100 - 142.36 = 706.69
        ProfitCalculation calculo = ProfitCalculator.calculate(
                new BigDecimal("100"), List.of(new BigDecimal("999")));

        assertThat(calculo.precioReventaEstimado()).isEqualByComparingTo(new BigDecimal("949.05"));
        assertThat(calculo.gananciaEstimada()).isEqualByComparingTo(new BigDecimal("706.69"));
    }

    @Test
    void permiteGananciaNegativaCuandoElPrecioDeCompraEsAlto() {
        ProfitCalculation calculo = ProfitCalculator.calculate(
                new BigDecimal("5000"), List.of(new BigDecimal("2000")));

        // reventa = 1900.00 ; costos = 285.00 ; ganancia = 1900 - 5000 - 285 = -3385.00
        assertThat(calculo.gananciaEstimada()).isEqualByComparingTo(new BigDecimal("-3385.00"));
    }

    @Test
    void rechazaListaDeComparablesVacia() {
        assertThatThrownBy(() -> ProfitCalculator.calculate(new BigDecimal("100"), List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
