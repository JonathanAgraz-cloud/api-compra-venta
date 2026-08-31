package com.jonypacheco.marketplace.alert.message;

import com.jonypacheco.marketplace.alert.AlertBatchSummary;
import com.jonypacheco.marketplace.analysis.AnalysisOutcome;
import com.jonypacheco.marketplace.analysis.OpportunityResult;
import com.jonypacheco.marketplace.analysis.pricing.ProfitCalculation;
import com.jonypacheco.marketplace.persistence.domain.entity.Listing;
import com.jonypacheco.marketplace.persistence.domain.enums.GananciaClasificacion;
import com.jonypacheco.marketplace.persistence.domain.enums.ZonaMerida;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AlertMessageFormatterTest {

    private Listing listingDeEjemplo() {
        Listing listing = new Listing("123456789", "iPhone 12 128GB", new BigDecimal("3000"), ZonaMerida.ALTABRISA,
                "https://www.facebook.com/marketplace/item/123456789/");
        listing.setCategoria("iPhone 12 128GB");
        return listing;
    }

    @Test
    void incluyeTodosLosCamposDeLaOportunidad() {
        Listing listing = listingDeEjemplo();
        ProfitCalculation calculo = new ProfitCalculation(new BigDecimal("5700.00"), new BigDecimal("2450.00"));
        OpportunityResult resultado = OpportunityResult.encontrada(listing, calculo, GananciaClasificacion.ALTA, 7);

        String mensaje = AlertMessageFormatter.format(resultado);

        assertThat(mensaje).contains("iPhone 12 128GB");
        assertThat(mensaje).contains("2,450.00");
        assertThat(mensaje).contains("5,700.00");
        assertThat(mensaje).contains("3,000.00");
        assertThat(mensaje).contains("ALTABRISA");
        assertThat(mensaje).contains("Comparables usados: 7");
        assertThat(mensaje).contains("https://www.facebook.com/marketplace/item/123456789/");
    }

    @Test
    void usaElEmojiCorrectoPorClasificacion() {
        Listing listing = listingDeEjemplo();
        ProfitCalculation calculo = new ProfitCalculation(new BigDecimal("1000.00"), new BigDecimal("600.00"));

        String mensajeBaja = AlertMessageFormatter.format(
                OpportunityResult.encontrada(listing, calculo, GananciaClasificacion.BAJA, 5));
        String mensajeMedia = AlertMessageFormatter.format(
                OpportunityResult.encontrada(listing, calculo, GananciaClasificacion.MEDIA, 5));
        String mensajeAlta = AlertMessageFormatter.format(
                OpportunityResult.encontrada(listing, calculo, GananciaClasificacion.ALTA, 5));

        assertThat(mensajeBaja).startsWith("🟢");
        assertThat(mensajeMedia).startsWith("🟡");
        assertThat(mensajeAlta).startsWith("🔴");
    }

    @Test
    void rechazaResultadosQueNoSonOportunidad() {
        Listing listing = listingDeEjemplo();
        OpportunityResult descartado = OpportunityResult.descartado(listing, AnalysisOutcome.PROFIT_TOO_LOW, 5);

        assertThatThrownBy(() -> AlertMessageFormatter.format(descartado))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void mensajeSinOportunidadesIncluyeElConteoDeNoOportunidades() {
        AlertBatchSummary resumen = new AlertBatchSummary(0, 0, 0, 41);

        String mensaje = AlertMessageFormatter.formatSinOportunidadesNuevas(resumen);

        assertThat(mensaje).contains("sin alertas nuevas");
        assertThat(mensaje).contains("Anuncios revisados sin oportunidad: 41");
        assertThat(mensaje).doesNotContain("ya se habian notificado");
        assertThat(mensaje).doesNotContain("Fallos de envio");
    }

    @Test
    void mensajeSinOportunidadesMencionaYaAlertadasYFallidasCuandoExisten() {
        AlertBatchSummary resumen = new AlertBatchSummary(0, 2, 1, 10);

        String mensaje = AlertMessageFormatter.formatSinOportunidadesNuevas(resumen);

        assertThat(mensaje).contains("ya se habian notificado antes: 2");
        assertThat(mensaje).contains("Fallos de envio (se reintentan solos la siguiente hora): 1");
    }
}
