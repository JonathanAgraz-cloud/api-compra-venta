package com.jonypacheco.marketplace.scraper.parsing;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Convierte el texto de precio que muestra Facebook Marketplace (ej.
 * "MX$1,500", "$999.50") a un {@link BigDecimal}. Logica pura, sin
 * dependencias externas: no lanza excepciones, nunca falla el flujo de
 * ingesta por un formato inesperado -- simplemente devuelve
 * {@link Optional#empty()} y el llamador decide que hacer (omitir la
 * tarjeta).
 */
public final class PriceParser {

    // Primer numero (con separador de miles opcional y decimales opcionales)
    // que aparezca en el texto, ej. "1,500.50" dentro de "MX$1,500.50".
    private static final Pattern NUMBER_PATTERN = Pattern.compile("[0-9][0-9,]*(?:\\.[0-9]+)?");

    // Non-breaking space (U+00A0): comun en texto scrapeado de paginas web
    // entre el simbolo de moneda y el numero. Se usa el escape unicode para
    // no depender de un caracter invisible literal en el fuente.
    private static final char NBSP = '\u00A0';

    private PriceParser() {
    }

    public static Optional<BigDecimal> parse(String rawPrice) {
        if (rawPrice == null) {
            return Optional.empty();
        }

        String cleaned = rawPrice.replace(NBSP, ' ').trim();
        if (cleaned.isEmpty()) {
            return Optional.empty();
        }

        Matcher matcher = NUMBER_PATTERN.matcher(cleaned);
        if (!matcher.find()) {
            // Cubre casos como "Gratis", "Free", texto vacio de precio, etc.
            return Optional.empty();
        }

        String numeroSinComas = matcher.group().replace(",", "");
        try {
            return Optional.of(new BigDecimal(numeroSinComas));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
