package com.jonypacheco.marketplace.scraper.parsing;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extrae el id numerico de un anuncio a partir de su URL de Facebook
 * Marketplace (ej. {@code https://www.facebook.com/marketplace/item/123456789/}),
 * con o sin parametros de query, con o sin slash final. Ese id es el
 * {@code facebook_id} que usa {@code listings} para deduplicar.
 */
public final class ListingIdExtractor {

    private static final Pattern ITEM_ID_PATTERN = Pattern.compile("/marketplace/item/(\\d+)");

    private ListingIdExtractor() {
    }

    public static Optional<String> extract(String url) {
        if (url == null) {
            return Optional.empty();
        }

        Matcher matcher = ITEM_ID_PATTERN.matcher(url);
        if (!matcher.find()) {
            return Optional.empty();
        }

        return Optional.of(matcher.group(1));
    }
}
