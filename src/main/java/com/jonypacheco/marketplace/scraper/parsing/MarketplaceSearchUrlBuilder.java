package com.jonypacheco.marketplace.scraper.parsing;

import com.jonypacheco.marketplace.persistence.domain.entity.SearchConfig;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Construye la URL de busqueda de Facebook Marketplace a partir de un
 * {@link SearchConfig}.
 *
 * <p><b>Advertencia:</b> el formato exacto (slug de ciudad, nombres de
 * parametros) no se puede verificar sin una sesion real logueada -- es un
 * placeholder razonable que hay que ajustar la primera vez que se corra
 * contra Facebook real (ver riesgos del modulo scraper). La ubicacion
 * efectiva de los resultados la determina la cuenta/sesion de Facebook del
 * bootstrap manual, no un parametro de esta URL -- por eso {@code zona} de
 * {@link SearchConfig} no se usa aqui: la asignacion de zona a cada
 * resultado se hace despues, por texto, con {@link ZonaMapper}.
 */
public final class MarketplaceSearchUrlBuilder {

    private static final String BASE_SEARCH_URL = "https://www.facebook.com/marketplace/merida/search";

    private MarketplaceSearchUrlBuilder() {
    }

    public static String build(SearchConfig config) {
        StringBuilder url = new StringBuilder(BASE_SEARCH_URL)
                .append("?query=")
                .append(encode(config.getPalabrasClave()));

        BigDecimal precioMin = config.getPrecioMin();
        if (precioMin != null) {
            url.append("&minPrice=").append(precioMin.toBigInteger());
        }

        BigDecimal precioMax = config.getPrecioMax();
        if (precioMax != null) {
            url.append("&maxPrice=").append(precioMax.toBigInteger());
        }

        return url.toString();
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
