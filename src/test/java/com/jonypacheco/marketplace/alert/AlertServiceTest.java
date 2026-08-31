package com.jonypacheco.marketplace.alert;

import com.jonypacheco.marketplace.alert.telegram.TelegramClient;
import com.jonypacheco.marketplace.analysis.AnalysisOutcome;
import com.jonypacheco.marketplace.analysis.OpportunityResult;
import com.jonypacheco.marketplace.analysis.pricing.ProfitCalculation;
import com.jonypacheco.marketplace.persistence.domain.entity.AlertSent;
import com.jonypacheco.marketplace.persistence.domain.entity.Listing;
import com.jonypacheco.marketplace.persistence.domain.enums.GananciaClasificacion;
import com.jonypacheco.marketplace.persistence.domain.enums.ZonaMerida;
import com.jonypacheco.marketplace.persistence.repository.AlertSentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlertServiceTest {

    @Mock
    private AlertSentRepository alertSentRepository;
    @Mock
    private TelegramClient telegramClient;

    private AlertService alertService;

    private Listing listingDeEjemplo() {
        Listing listing = new Listing("123456789", "iPhone 12 128GB", new BigDecimal("3000"), ZonaMerida.ALTABRISA,
                "https://www.facebook.com/marketplace/item/123456789/");
        listing.setCategoria("iPhone 12 128GB");
        ReflectionTestUtils.setField(listing, "id", 10L);
        return listing;
    }

    private OpportunityResult oportunidadEncontrada(Listing listing) {
        ProfitCalculation calculo = new ProfitCalculation(new BigDecimal("5700.00"), new BigDecimal("2450.00"));
        return OpportunityResult.encontrada(listing, calculo, GananciaClasificacion.ALTA, 7);
    }

    @Test
    void devuelveNotAnOpportunityYNoTocaNadaSiNoEsOportunidad() {
        alertService = new AlertService(alertSentRepository, telegramClient);
        Listing listing = listingDeEjemplo();
        OpportunityResult descartado = OpportunityResult.descartado(listing, AnalysisOutcome.PROFIT_TOO_LOW, 5);

        AlertOutcome outcome = alertService.sendAlert(descartado);

        assertThat(outcome).isEqualTo(AlertOutcome.NOT_AN_OPPORTUNITY);
        verify(alertSentRepository, never()).existsByListing_Id(any());
        verify(telegramClient, never()).sendMessage(anyString());
    }

    @Test
    void devuelveAlreadyAlertedSiYaExisteUnaAlertaParaElListing() {
        alertService = new AlertService(alertSentRepository, telegramClient);
        Listing listing = listingDeEjemplo();
        when(alertSentRepository.existsByListing_Id(10L)).thenReturn(true);

        AlertOutcome outcome = alertService.sendAlert(oportunidadEncontrada(listing));

        assertThat(outcome).isEqualTo(AlertOutcome.ALREADY_ALERTED);
        verify(telegramClient, never()).sendMessage(anyString());
    }

    @Test
    void devuelveSendFailedYNoPersisteNadaSiFallaElEnvio() {
        alertService = new AlertService(alertSentRepository, telegramClient);
        Listing listing = listingDeEjemplo();
        when(alertSentRepository.existsByListing_Id(10L)).thenReturn(false);
        when(telegramClient.sendMessage(anyString())).thenReturn(false);

        AlertOutcome outcome = alertService.sendAlert(oportunidadEncontrada(listing));

        assertThat(outcome).isEqualTo(AlertOutcome.SEND_FAILED);
        verify(alertSentRepository, never()).save(any());
    }

    @Test
    void guardaLaAlertaConLosCamposCorrectosCuandoElEnvioTieneExito() {
        alertService = new AlertService(alertSentRepository, telegramClient);
        Listing listing = listingDeEjemplo();
        when(alertSentRepository.existsByListing_Id(10L)).thenReturn(false);
        when(telegramClient.sendMessage(anyString())).thenReturn(true);

        AlertOutcome outcome = alertService.sendAlert(oportunidadEncontrada(listing));

        assertThat(outcome).isEqualTo(AlertOutcome.SENT);
        ArgumentCaptor<AlertSent> captor = ArgumentCaptor.forClass(AlertSent.class);
        verify(alertSentRepository).save(captor.capture());
        AlertSent guardada = captor.getValue();
        assertThat(guardada.getListing()).isEqualTo(listing);
        assertThat(guardada.getPrecioCompra()).isEqualByComparingTo(new BigDecimal("3000"));
        assertThat(guardada.getPrecioReventaEstimado()).isEqualByComparingTo(new BigDecimal("5700.00"));
        assertThat(guardada.getGananciaEstimada()).isEqualByComparingTo(new BigDecimal("2450.00"));
        assertThat(guardada.getClasificacion()).isEqualTo(GananciaClasificacion.ALTA);
        assertThat(guardada.getComparablesUsados()).isEqualTo(7);
        assertThat(guardada.getMensajeTelegram()).isNotBlank();
        assertThat(guardada.isEnviadoExitosamente()).isTrue();
    }

    @Test
    void tratasUnaViolacionDeIntegridadAlGuardarComoAlreadyAlerted() {
        alertService = new AlertService(alertSentRepository, telegramClient);
        Listing listing = listingDeEjemplo();
        when(alertSentRepository.existsByListing_Id(10L)).thenReturn(false);
        when(telegramClient.sendMessage(anyString())).thenReturn(true);
        when(alertSentRepository.save(any())).thenThrow(new DataIntegrityViolationException("duplicate"));

        AlertOutcome outcome = alertService.sendAlert(oportunidadEncontrada(listing));

        assertThat(outcome).isEqualTo(AlertOutcome.ALREADY_ALERTED);
    }

    @Test
    void processOpportunitiesAcumulaElResumenPorCadaOutcome() {
        alertService = new AlertService(alertSentRepository, telegramClient);
        Listing listingNuevo = listingDeEjemplo();
        Listing listingYaAlertado = listingDeEjemplo();
        ReflectionTestUtils.setField(listingYaAlertado, "id", 11L);
        Listing listingFalla = listingDeEjemplo();
        ReflectionTestUtils.setField(listingFalla, "id", 12L);

        when(alertSentRepository.existsByListing_Id(10L)).thenReturn(false);
        when(alertSentRepository.existsByListing_Id(11L)).thenReturn(true);
        when(alertSentRepository.existsByListing_Id(12L)).thenReturn(false);
        when(telegramClient.sendMessage(anyString())).thenReturn(true, false);

        List<OpportunityResult> resultados = List.of(
                oportunidadEncontrada(listingNuevo),
                oportunidadEncontrada(listingYaAlertado),
                oportunidadEncontrada(listingFalla),
                OpportunityResult.descartado(listingDeEjemplo(), AnalysisOutcome.INSUFFICIENT_COMPARABLES, 3));

        AlertBatchSummary resumen = alertService.processOpportunities(resultados);

        assertThat(resumen.enviadas()).isEqualTo(1);
        assertThat(resumen.yaAlertadas()).isEqualTo(1);
        assertThat(resumen.fallidas()).isEqualTo(1);
        assertThat(resumen.noOportunidades()).isEqualTo(1);
    }

    @Test
    void mandaAvisoDeSinAlertasNuevasCuandoNingunaOportunidadSeAlerto() {
        alertService = new AlertService(alertSentRepository, telegramClient);
        Listing listing = listingDeEjemplo();
        when(telegramClient.sendMessage(anyString())).thenReturn(true);

        List<OpportunityResult> resultados = List.of(
                OpportunityResult.descartado(listing, AnalysisOutcome.PROFIT_TOO_LOW, 5));

        AlertBatchSummary resumen = alertService.processOpportunities(resultados);

        assertThat(resumen.enviadas()).isZero();
        verify(telegramClient, times(1)).sendMessage(anyString());
    }

    @Test
    void noMandaAvisoDeSinAlertasNuevasSiSeEnvioAlMenosUnaOportunidad() {
        alertService = new AlertService(alertSentRepository, telegramClient);
        Listing listing = listingDeEjemplo();
        when(alertSentRepository.existsByListing_Id(10L)).thenReturn(false);
        when(telegramClient.sendMessage(anyString())).thenReturn(true);

        AlertBatchSummary resumen = alertService.processOpportunities(List.of(oportunidadEncontrada(listing)));

        assertThat(resumen.enviadas()).isEqualTo(1);
        verify(telegramClient, times(1)).sendMessage(anyString());
    }

    @Test
    void noFallaSiElAvisoDeSinAlertasNuevasNoSePudoEnviar() {
        alertService = new AlertService(alertSentRepository, telegramClient);
        Listing listing = listingDeEjemplo();
        when(telegramClient.sendMessage(anyString())).thenReturn(false);

        AlertBatchSummary resumen = alertService.processOpportunities(
                List.of(OpportunityResult.descartado(listing, AnalysisOutcome.PROFIT_TOO_LOW, 5)));

        assertThat(resumen.enviadas()).isZero();
    }
}
