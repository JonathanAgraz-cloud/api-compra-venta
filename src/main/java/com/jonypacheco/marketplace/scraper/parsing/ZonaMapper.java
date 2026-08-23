package com.jonypacheco.marketplace.scraper.parsing;

import com.jonypacheco.marketplace.persistence.domain.enums.ZonaMerida;

import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Mapea el texto de ubicacion que muestra Facebook Marketplace (ej. "Cholul,
 * Yuc.") a {@link ZonaMerida} por coincidencia de texto normalizado (sin
 * acentos, minusculas). Facebook Marketplace no permite filtrar busquedas
 * por colonia, solo por ciudad/radio -- este mapeo es la unica forma de
 * saber si un anuncio cae en una de las zonas prioritarias. Nunca lanza
 * excepciones ni devuelve null: si no matchea ninguna zona conocida,
 * devuelve {@link ZonaMerida#OTRA_ZONA}.
 */
public final class ZonaMapper {

    private static final Pattern DIACRITICS = Pattern.compile("\\p{M}");

    // Alias en orden de especificidad: se evita un alias generico como
    // "temozon" a secas para no confundir "Temozon Norte" con la zona real
    // de Merida "Temozon Sur", que no esta en nuestras zonas prioritarias.
    private static final Map<ZonaMerida, String[]> ALIASES = new LinkedHashMap<>();

    static {
        ALIASES.put(ZonaMerida.ALTABRISA, new String[] {"altabrisa"});
        ALIASES.put(ZonaMerida.TEMOZON_NORTE, new String[] {"temozon norte"});
        ALIASES.put(ZonaMerida.CHOLUL, new String[] {"cholul"});
        ALIASES.put(ZonaMerida.DZITYA, new String[] {"dzitya"});
        ALIASES.put(ZonaMerida.YUCATAN_COUNTRY_CLUB, new String[] {"yucatan country club", "country club"});
        ALIASES.put(ZonaMerida.FRANCISCO_DE_MONTEJO, new String[] {"francisco de montejo", "montejo"});
    }

    private ZonaMapper() {
    }

    public static ZonaMerida map(String ubicacionRaw) {
        if (ubicacionRaw == null || ubicacionRaw.isBlank()) {
            return ZonaMerida.OTRA_ZONA;
        }

        String normalizado = normalizar(ubicacionRaw);

        for (Map.Entry<ZonaMerida, String[]> entry : ALIASES.entrySet()) {
            for (String alias : entry.getValue()) {
                if (normalizado.contains(alias)) {
                    return entry.getKey();
                }
            }
        }

        return ZonaMerida.OTRA_ZONA;
    }

    private static String normalizar(String texto) {
        String descompuesto = Normalizer.normalize(texto, Normalizer.Form.NFD);
        String sinAcentos = DIACRITICS.matcher(descompuesto).replaceAll("");
        return sinAcentos.toLowerCase();
    }
}
