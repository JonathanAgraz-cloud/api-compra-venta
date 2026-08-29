package com.jonypacheco.marketplace.scraper.parsing;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Separa el {@code innerText} de una tarjeta de resultado de Marketplace en
 * {@code [precio, titulo, ubicacion]}. Extraido de {@code MarketplaceSearchNavigator}
 * para que esta heuristica sea 100% testeable sin Playwright (ver
 * arquitectura del modulo scraper: puro vs. con efectos).
 *
 * <p>Reglas, ajustadas con tarjetas reales de Facebook Marketplace
 * (2026-08-29):
 * <ul>
 *   <li>La ubicacion es siempre la ultima linea no vacia.</li>
 *   <li>Puede haber MAS DE UNA linea con pinta de precio cuando el anuncio
 *       tiene descuento (precio actual + "reduced from" el precio
 *       original) -- se usa la PRIMERA como precio real y se descartan
 *       TODAS las lineas de precio al buscar el titulo, no solo la
 *       primera.</li>
 *   <li>Puede haber una linea de badge antes del precio (ej.
 *       {@code "Just listed"}) que no es parte del titulo.</li>
 * </ul>
 */
public final class ListingCardTextParser {

    // Badges conocidos que Facebook antepone al precio en la tarjeta,
    // confirmados contra datos reales. Lista corta a proposito -- se agrega
    // un badge aqui solo cuando se confirma con datos reales, no por
    // adivinar.
    private static final Set<String> BADGES_CONOCIDOS = Set.of("Just listed");

    private ListingCardTextParser() {
    }

    public record CardText(String precio, String titulo, String ubicacion) {
    }

    public static CardText parse(String innerText) {
        List<String> lineas = new ArrayList<>();
        if (innerText != null) {
            for (String linea : innerText.split("\\R")) {
                String limpia = linea.trim();
                if (!limpia.isEmpty()) {
                    lineas.add(limpia);
                }
            }
        }

        String ubicacion = lineas.isEmpty() ? "" : lineas.remove(lineas.size() - 1);

        List<String> lineasDePrecio = lineas.stream()
                .filter(linea -> linea.matches(".*\\$\\s?[0-9].*"))
                .toList();
        String precio = lineasDePrecio.isEmpty() ? "" : lineasDePrecio.get(0);
        lineas.removeAll(lineasDePrecio);
        lineas.removeIf(BADGES_CONOCIDOS::contains);

        String titulo = lineas.isEmpty() ? "" : lineas.get(0);

        return new CardText(precio, titulo, ubicacion);
    }
}
