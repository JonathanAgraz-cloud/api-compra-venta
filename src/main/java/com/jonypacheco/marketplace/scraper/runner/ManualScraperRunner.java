package com.jonypacheco.marketplace.scraper.runner;

import com.jonypacheco.marketplace.scraper.MarketplaceScraperService;
import com.jonypacheco.marketplace.scraper.ScraperRunSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Dispara una corrida real del scraper al arrancar la app, SOLO si el
 * perfil {@code scraper-manual} esta activo explicitamente (ej.
 * {@code SPRING_PROFILES_ACTIVE=dev,scraper-manual}). Pensado para que
 * Jony pueda probar el scraper contra Facebook real desde su maquina antes
 * de que exista el modulo de scheduler -- nunca se dispara en un arranque
 * normal (perfil {@code dev} o {@code prod} solos).
 */
@Component
@Profile("scraper-manual")
public class ManualScraperRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ManualScraperRunner.class);

    private final MarketplaceScraperService scraperService;

    public ManualScraperRunner(MarketplaceScraperService scraperService) {
        this.scraperService = scraperService;
    }

    @Override
    public void run(String... args) {
        log.info("Iniciando corrida manual de prueba del scraper (perfil scraper-manual activo)...");
        ScraperRunSummary resumen = scraperService.scrapeAllActiveConfigs();
        log.info("Corrida manual terminada: {}", resumen);
    }
}
