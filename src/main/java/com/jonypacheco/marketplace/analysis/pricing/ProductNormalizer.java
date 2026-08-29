package com.jonypacheco.marketplace.analysis.pricing;

import java.text.Normalizer;
import java.util.regex.Pattern;

/**
 * Normaliza la {@code categoria} de un {@link com.jonypacheco.marketplace.persistence.domain.entity.Listing}
 * (que viene del {@code SearchConfig} definido por Jony, ej. "iPhone 12 128GB")
 * a una clave de comparacion estable ({@code producto_normalizado} en la
 * tabla {@code comparables}): sin acentos, en minusculas, sin espacios
 * duplicados ni al inicio/final. No hace matching difuso de titulos -- dos
 * anuncios solo se consideran "el mismo producto" si comparten la misma
 * categoria, ya que esa es una decision explicita de Jony al crear el
 * {@code SearchConfig}, no texto libre scrapeado.
 */
public final class ProductNormalizer {

    private static final Pattern DIACRITICS = Pattern.compile("\\p{M}");
    private static final Pattern ESPACIOS_MULTIPLES = Pattern.compile("\\s+");

    private ProductNormalizer() {
    }

    public static String normalize(String categoria) {
        if (categoria == null || categoria.isBlank()) {
            return null;
        }

        String descompuesto = Normalizer.normalize(categoria, Normalizer.Form.NFD);
        String sinAcentos = DIACRITICS.matcher(descompuesto).replaceAll("");
        String colapsado = ESPACIOS_MULTIPLES.matcher(sinAcentos.trim()).replaceAll(" ");
        return colapsado.toLowerCase();
    }
}
