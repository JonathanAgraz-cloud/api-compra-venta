package com.jonypacheco.marketplace.analysis.pricing;

import com.jonypacheco.marketplace.persistence.domain.enums.GananciaClasificacion;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Aplica el umbral minimo de alerta y la clasificacion por rango de ganancia
 * (ver CLAUDE.md): menos de $500 se descarta (vacio), $500-$999 BAJA,
 * $1,000-$1,999 MEDIA, $2,000+ ALTA.
 */
public final class GananciaClasificador {

    private static final BigDecimal MINIMO_ALERTA = new BigDecimal("500");
    private static final BigDecimal UMBRAL_MEDIA = new BigDecimal("1000");
    private static final BigDecimal UMBRAL_ALTA = new BigDecimal("2000");

    private GananciaClasificador() {
    }

    public static Optional<GananciaClasificacion> clasificar(BigDecimal gananciaEstimada) {
        if (gananciaEstimada.compareTo(MINIMO_ALERTA) < 0) {
            return Optional.empty();
        }
        if (gananciaEstimada.compareTo(UMBRAL_ALTA) >= 0) {
            return Optional.of(GananciaClasificacion.ALTA);
        }
        if (gananciaEstimada.compareTo(UMBRAL_MEDIA) >= 0) {
            return Optional.of(GananciaClasificacion.MEDIA);
        }
        return Optional.of(GananciaClasificacion.BAJA);
    }
}
