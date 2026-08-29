package com.jonypacheco.marketplace.scraper.parsing;

/**
 * Convierte el {@code href} relativo que trae el DOM de Facebook (ej.
 * {@code /marketplace/item/123/?ref=search}) en una URL absoluta y
 * clickeable -- confirmado con datos reales que Facebook renderiza estos
 * enlaces como rutas relativas, no absolutas. Sin esto, la URL guardada en
 * {@code listings.url} (el "enlace del anuncio" que exige arquitectura-tecnica.md
 * para el mensaje de Telegram) no serviría para nada.
 */
public final class FacebookUrlResolver {

    private static final String ORIGIN = "https://www.facebook.com";

    private FacebookUrlResolver() {
    }

    public static String resolve(String href) {
        if (href == null || href.isBlank()) {
            return href;
        }
        if (href.startsWith("http://") || href.startsWith("https://")) {
            return href;
        }
        return href.startsWith("/") ? ORIGIN + href : ORIGIN + "/" + href;
    }
}
