package com.jonypacheco.marketplace.scraper;

/** Resumen de una corrida completa de {@code MarketplaceScraperService}. */
public record ScraperRunSummary(
        int configsProcesadas,
        int configsConError,
        int listingsNuevos,
        int listingsActualizados,
        int listingsOmitidos,
        boolean abortadaPorSesion) {

    static ScraperRunSummary vacio() {
        return new ScraperRunSummary(0, 0, 0, 0, 0, false);
    }
}
