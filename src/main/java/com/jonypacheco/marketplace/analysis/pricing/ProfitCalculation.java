package com.jonypacheco.marketplace.analysis.pricing;

import java.math.BigDecimal;

/**
 * Resultado puro del calculo de ganancia para un listing, sin clasificar
 * todavia (ver {@link GananciaClasificador}).
 */
public record ProfitCalculation(BigDecimal precioReventaEstimado, BigDecimal gananciaEstimada) {
}
