package com.jonypacheco.marketplace.scheduler;

import com.jonypacheco.marketplace.alert.AlertBatchSummary;
import com.jonypacheco.marketplace.alert.AlertService;
import com.jonypacheco.marketplace.analysis.OpportunityAnalysisService;
import com.jonypacheco.marketplace.analysis.OpportunityResult;
import com.jonypacheco.marketplace.persistence.domain.entity.Listing;
import com.jonypacheco.marketplace.persistence.domain.enums.ZonaMerida;
import com.jonypacheco.marketplace.scraper.MarketplaceScraperService;
import com.jonypacheco.marketplace.scraper.ScraperRunSummary;
import com.jonypacheco.marketplace.scraper.ScraperSessionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpportunityPipelineSchedulerTest {

    @Mock
    private MarketplaceScraperService scraperService;
    @Mock
    private OpportunityAnalysisService analysisService;
    @Mock
    private AlertService alertService;

    private final ScraperRunSummary resumenScraper = new ScraperRunSummary(1, 0, 1, 0, 0, false);

    private List<OpportunityResult> listaDeEjemplo() {
        Listing listing = new Listing("111", "iPhone 12", new BigDecimal("3000"), ZonaMerida.ALTABRISA,
                "https://www.facebook.com/marketplace/item/111/");
        return List.of(OpportunityResult.descartado(listing, com.jonypacheco.marketplace.analysis.AnalysisOutcome.INSUFFICIENT_COMPARABLES, 2));
    }

    @Test
    void corridaNormalLlamaALosTresOrquestadoresEnOrden() {
        OpportunityPipelineScheduler scheduler = new OpportunityPipelineScheduler(
                scraperService, analysisService, alertService, false);
        List<OpportunityResult> resultados = listaDeEjemplo();
        when(scraperService.scrapeAllActiveConfigs()).thenReturn(resumenScraper);
        when(analysisService.analyzeAllActiveListings()).thenReturn(resultados);
        when(alertService.processOpportunities(resultados)).thenReturn(new AlertBatchSummary(0, 0, 0, 1));

        scheduler.runOpportunityPipeline();

        verify(scraperService).scrapeAllActiveConfigs();
        verify(analysisService).analyzeAllActiveListings();
        verify(alertService).processOpportunities(eq(resultados));
    }

    @Test
    void sesionInvalidaAbortaAntesDeAnalisisYAlertas() {
        OpportunityPipelineScheduler scheduler = new OpportunityPipelineScheduler(
                scraperService, analysisService, alertService, false);
        when(scraperService.scrapeAllActiveConfigs()).thenThrow(new ScraperSessionException("sesion vencida"));

        scheduler.runOpportunityPipeline();

        verify(analysisService, never()).analyzeAllActiveListings();
        verify(alertService, never()).processOpportunities(any());
    }

    @Test
    void fallaInesperadaEnScraperAbortaAntesDeAnalisisYAlertas() {
        OpportunityPipelineScheduler scheduler = new OpportunityPipelineScheduler(
                scraperService, analysisService, alertService, false);
        when(scraperService.scrapeAllActiveConfigs()).thenThrow(new RuntimeException("fallo inesperado"));

        scheduler.runOpportunityPipeline();

        verify(analysisService, never()).analyzeAllActiveListings();
        verify(alertService, never()).processOpportunities(any());
    }

    @Test
    void fallaInesperadaEnAnalisisNoPropagaLaExcepcion() {
        OpportunityPipelineScheduler scheduler = new OpportunityPipelineScheduler(
                scraperService, analysisService, alertService, false);
        when(scraperService.scrapeAllActiveConfigs()).thenReturn(resumenScraper);
        when(analysisService.analyzeAllActiveListings()).thenThrow(new RuntimeException("fallo de analisis"));

        assertThatCode(scheduler::runOpportunityPipeline).doesNotThrowAnyException();
        verify(alertService, never()).processOpportunities(any());
    }

    @Test
    void fallaInesperadaEnAlertasNoPropagaLaExcepcion() {
        OpportunityPipelineScheduler scheduler = new OpportunityPipelineScheduler(
                scraperService, analysisService, alertService, false);
        List<OpportunityResult> resultados = listaDeEjemplo();
        when(scraperService.scrapeAllActiveConfigs()).thenReturn(resumenScraper);
        when(analysisService.analyzeAllActiveListings()).thenReturn(resultados);
        when(alertService.processOpportunities(resultados)).thenThrow(new RuntimeException("fallo de alertas"));

        assertThatCode(scheduler::runOpportunityPipeline).doesNotThrowAnyException();
    }

    @Test
    void runScheduledPipelineNoHaceNadaSiElSchedulerEstaDeshabilitado() {
        OpportunityPipelineScheduler scheduler = new OpportunityPipelineScheduler(
                scraperService, analysisService, alertService, false);

        scheduler.runScheduledPipeline();

        verify(scraperService, never()).scrapeAllActiveConfigs();
    }

    @Test
    void runScheduledPipelineEjecutaElPipelineSiElSchedulerEstaHabilitado() {
        OpportunityPipelineScheduler scheduler = new OpportunityPipelineScheduler(
                scraperService, analysisService, alertService, true);
        when(scraperService.scrapeAllActiveConfigs()).thenReturn(resumenScraper);
        when(analysisService.analyzeAllActiveListings()).thenReturn(List.of());
        when(alertService.processOpportunities(List.of())).thenReturn(new AlertBatchSummary(0, 0, 0, 0));

        scheduler.runScheduledPipeline();

        verify(scraperService).scrapeAllActiveConfigs();
    }
}
