package com.jonypacheco.marketplace.analysis.pricing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

/**
 * Aplica la formula de ganancia estimada (ver CLAUDE.md / arquitectura-tecnica.md
 * seccion 3):
 * <pre>
 * precioComparableMasBarato = min(preciosComparables)
 * precioReventaEstimado     = precioComparableMasBarato * 0.95
 * costosReventa             = precioReventaEstimado * 0.15   (15% sobre el precio de reventa, no sobre la compra)
 * gananciaEstimada          = precioReventaEstimado - precioCompra - costosReventa
 * </pre>
 * Redondeo {@link RoundingMode#HALF_UP} a 2 decimales en cada paso, consistente
 * con las columnas {@code DECIMAL(10,2)}. No valida la regla de "minimo 5
 * comparables" -- esa es responsabilidad del llamador
 * ({@code OpportunityAnalysisService}); esta clase solo hace la aritmetica.
 */
public final class ProfitCalculator {

    private static final BigDecimal FACTOR_REVENTA = new BigDecimal("0.95");
    private static final BigDecimal FACTOR_COSTOS_REVENTA = new BigDecimal("0.15");

    private ProfitCalculator() {
    }

    public static ProfitCalculation calculate(BigDecimal precioCompra, List<BigDecimal> preciosComparables) {
        if (preciosComparables == null || preciosComparables.isEmpty()) {
            throw new IllegalArgumentException("preciosComparables no puede estar vacio");
        }

        BigDecimal precioComparableMasBarato = preciosComparables.stream()
                .min(Comparator.naturalOrder())
                .orElseThrow();

        BigDecimal precioReventaEstimado = redondear(precioComparableMasBarato.multiply(FACTOR_REVENTA));
        BigDecimal costosReventa = redondear(precioReventaEstimado.multiply(FACTOR_COSTOS_REVENTA));
        BigDecimal gananciaEstimada = redondear(precioReventaEstimado.subtract(precioCompra).subtract(costosReventa));

        return new ProfitCalculation(precioReventaEstimado, gananciaEstimada);
    }

    private static BigDecimal redondear(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }
}
