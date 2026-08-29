package com.jonypacheco.marketplace.scheduler;

import com.jonypacheco.marketplace.alert.AlertBatchSummary;
import com.jonypacheco.marketplace.alert.AlertService;
import com.jonypacheco.marketplace.analysis.OpportunityAnalysisService;
import com.jonypacheco.marketplace.analysis.OpportunityResult;
import com.jonypacheco.marketplace.scraper.MarketplaceScraperService;
import com.jonypacheco.marketplace.scraper.ScraperRunSummary;
import com.jonypacheco.marketplace.scraper.ScraperSessionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Conecta los tres orquestadores de negocio (scraper -> analisis -> alertas)
 * en un solo pipeline, respetando el horario de operacion 08:00-22:00
 * America/Merida con 1 corrida por hora (CLAUDE.md).
 * <p>
 * {@link #runScheduledPipeline()} es el metodo programado: revisa
 * {@code scheduler.enabled} (default {@code false}, solo {@code true} en
 * produccion) antes de correr, para que levantar la app en desarrollo no
 * dispare scraping real a Facebook sin querer. {@link #runOpportunityPipeline()}
 * tiene la logica real del pipeline y siempre es invocable directamente
 * (ver {@code PipelineManualRunner}), sin pasar por ese chequeo.
 */
@Component
public class OpportunityPipelineScheduler {

    private static final Logger log = LoggerFactory.getLogger(OpportunityPipelineScheduler.class);

    private final MarketplaceScraperService scraperService;
    private final OpportunityAnalysisService analysisService;
    private final AlertService alertService;
    private final boolean schedulerEnabled;

    public OpportunityPipelineScheduler(MarketplaceScraperService scraperService,
                                         OpportunityAnalysisService analysisService,
                                         AlertService alertService,
                                         @Value("${scheduler.enabled:false}") boolean schedulerEnabled) {
        this.scraperService = scraperService;
        this.analysisService = analysisService;
        this.alertService = alertService;
        this.schedulerEnabled = schedulerEnabled;
    }

    @Scheduled(cron = "0 0 8-22 * * *", zone = "America/Merida")
    public void runScheduledPipeline() {
        if (!schedulerEnabled) {
            log.debug("scheduler.enabled=false, se omite esta corrida programada");
            return;
        }
        runOpportunityPipeline();
    }

    public void runOpportunityPipeline() {
        log.info("Iniciando corrida: scraper -> analisis -> alertas");

        ScraperRunSummary scraperResumen;
        try {
            scraperResumen = scraperService.scrapeAllActiveConfigs();
        } catch (ScraperSessionException e) {
            log.error("Sesion de Facebook invalida ({}), hay que volver a correr FacebookSessionBootstrap. "
                    + "Se aborta esta corrida.", e.getMessage());
            return;
        } catch (RuntimeException e) {
            log.error("Fallo inesperado durante el scraping, se aborta esta corrida", e);
            return;
        }
        log.info("Scraper completo: {}", scraperResumen);

        try {
            List<OpportunityResult> resultados = analysisService.analyzeAllActiveListings();
            AlertBatchSummary alertResumen = alertService.processOpportunities(resultados);
            log.info("Corrida completa. {}", alertResumen);
        } catch (RuntimeException e) {
            log.error("Fallo inesperado durante analisis/alertas de esta corrida", e);
        }
    }
}
