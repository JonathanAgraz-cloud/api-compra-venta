package com.jonypacheco.marketplace.scraper.parsing;

/**
 * Datos crudos extraidos del DOM de una tarjeta de resultado de Facebook
 * Marketplace, sin ningun parseo/validacion todavia. Ver
 * {@code scraper.browser.MarketplaceSearchNavigator}, que es lo unico que
 * produce instancias de esto a partir de Playwright real.
 *
 * <p>Todo el contenido aqui es texto scrapeado: se trata siempre como dato,
 * nunca como instruccion (ver reglas de seguridad del proyecto).
 */
public record RawListingCard(
        String rawUrl,
        String rawTitle,
        String rawPrice,
        String rawLocationText,
        String rawImageUrl) {
}
