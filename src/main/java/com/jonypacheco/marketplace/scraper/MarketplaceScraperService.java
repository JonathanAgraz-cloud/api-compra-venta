package com.jonypacheco.marketplace.scraper;

import com.jonypacheco.marketplace.persistence.domain.entity.SearchConfig;
import com.jonypacheco.marketplace.persistence.repository.SearchConfigRepository;
import com.jonypacheco.marketplace.scraper.browser.BrowserSession;
import com.jonypacheco.marketplace.scraper.browser.MarketplaceSearchNavigator;
import com.jonypacheco.marketplace.scraper.browser.PlaywrightBrowserManager;
import com.jonypacheco.marketplace.scraper.config.ScraperProperties;
import com.jonypacheco.marketplace.scraper.ingest.IngestOutcome;
import com.jonypacheco.marketplace.scraper.ingest.ListingIngestService;
import com.jonypacheco.marketplace.scraper.parsing.MarketplaceSearchUrlBuilder;
import com.jonypacheco.marketplace.scraper.parsing.RawListingCard;
import com.microsoft.playwright.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Orquestador principal del scraper: por cada {@code SearchConfig} activa,
 * busca en Facebook Marketplace y guarda/actualiza los anuncios encontrados.
 * Se invoca programaticamente (sin {@code @Scheduled} propio) -- un modulo
 * futuro de scheduler decide cuando y con que frecuencia llamarlo.
 */
@Service
public class MarketplaceScraperService {

    private static final Logger log = LoggerFactory.getLogger(MarketplaceScraperService.class);

    private final SearchConfigRepository searchConfigRepository;
    private final PlaywrightBrowserManager browserManager;
    private final MarketplaceSearchNavigator navigator;
    private final ListingIngestService ingestService;
    private final ScraperProperties properties;

    public MarketplaceScraperService(SearchConfigRepository searchConfigRepository,
                                      PlaywrightBrowserManager browserManager,
                                      MarketplaceSearchNavigator navigator,
                                      ListingIngestService ingestService,
                                      ScraperProperties properties) {
        this.searchConfigRepository = searchConfigRepository;
        this.browserManager = browserManager;
        this.navigator = navigator;
        this.ingestService = ingestService;
        this.properties = properties;
    }

    public ScraperRunSummary scrapeAllActiveConfigs() {
        List<SearchConfig> configs = searchConfigRepository.findByActivoTrue();
        if (configs.isEmpty()) {
            log.info("No hay search_configs activas, no se abre el navegador.");
            return ScraperRunSummary.vacio();
        }

        int configsProcesadas = 0;
        int configsConError = 0;
        int listingsNuevos = 0;
        int listingsActualizados = 0;
        int listingsOmitidos = 0;

        try (BrowserSession session = browserManager.open()) {
            boolean primera = true;
            for (SearchConfig config : configs) {
                if (!primera) {
                    delayEntreConfigs();
                }
                primera = false;

                log.info("Procesando config '{}' (zona declarada: {})", config.getNombre(), config.getZona());
                String url = MarketplaceSearchUrlBuilder.build(config);

                Page page = session.newPage();
                try {
                    List<RawListingCard> tarjetas = navigator.search(page, url, properties.getMaxResultsPerSearch());
                    configsProcesadas++;
                    for (RawListingCard tarjeta : tarjetas) {
                        IngestOutcome outcome = ingestService.ingest(tarjeta, config);
                        switch (outcome) {
                            case NEW -> listingsNuevos++;
                            case UPDATED -> listingsActualizados++;
                            case SKIPPED_INVALID_PRICE, SKIPPED_INVALID_ID -> listingsOmitidos++;
                        }
                    }
                } catch (ScraperSessionException e) {
                    throw e;
                } catch (Exception e) {
                    configsConError++;
                    log.warn("Fallo procesando config '{}', se continua con la siguiente: {}",
                            config.getNombre(), e.getMessage());
                } finally {
                    page.close();
                }
            }
        } catch (ScraperSessionException e) {
            ScraperRunSummary parcial = new ScraperRunSummary(configsProcesadas, configsConError,
                    listingsNuevos, listingsActualizados, listingsOmitidos, true);
            log.error("Sesion de Facebook invalida o expirada -- abortando corrida completa. "
                    + "Se requiere volver a ejecutar FacebookSessionBootstrap manualmente. Resumen parcial: {}",
                    resumenTexto(parcial), e);
            throw e;
        }

        ScraperRunSummary resumen = new ScraperRunSummary(configsProcesadas, configsConError,
                listingsNuevos, listingsActualizados, listingsOmitidos, false);
        log.info(resumenTexto(resumen));
        return resumen;
    }

    private void delayEntreConfigs() {
        long delayMs = ThreadLocalRandom.current().nextLong(properties.getDelayMinMs(), properties.getDelayMaxMs());
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrumpido durante el delay entre busquedas del scraper", e);
        }
    }

    private String resumenTexto(ScraperRunSummary resumen) {
        return "Scraping finalizado: %d configs, %d con error, %d nuevos, %d actualizados, %d omitidos"
                .formatted(resumen.configsProcesadas(), resumen.configsConError(), resumen.listingsNuevos(),
                        resumen.listingsActualizados(), resumen.listingsOmitidos());
    }
}
