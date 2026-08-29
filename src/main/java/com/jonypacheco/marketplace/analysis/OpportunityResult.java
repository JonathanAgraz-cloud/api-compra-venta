package com.jonypacheco.marketplace.analysis;

import com.jonypacheco.marketplace.analysis.pricing.ProfitCalculation;
import com.jonypacheco.marketplace.persistence.domain.entity.Listing;
import com.jonypacheco.marketplace.persistence.domain.enums.GananciaClasificacion;

import java.util.Optional;

/**
 * Resultado de {@link OpportunityAnalysisService#analyzeListing(Listing)}.
 * Cuando {@code outcome} no es {@link AnalysisOutcome#OPPORTUNITY_FOUND},
 * {@code calculo}/{@code clasificacion} vienen vacios y {@code comparablesUsados}
 * refleja cuantos comparables se encontraron (util para depurar
 * {@code INSUFFICIENT_COMPARABLES}). No persiste nada -- eso es
 * responsabilidad del futuro modulo de alertas.
 */
public record OpportunityResult(
        Listing listing,
        AnalysisOutcome outcome,
        Optional<ProfitCalculation> calculo,
        Optional<GananciaClasificacion> clasificacion,
        int comparablesUsados) {

    public static OpportunityResult descartado(Listing listing, AnalysisOutcome outcome) {
        return new OpportunityResult(listing, outcome, Optional.empty(), Optional.empty(), 0);
    }

    public static OpportunityResult descartado(Listing listing, AnalysisOutcome outcome, int comparablesUsados) {
        return new OpportunityResult(listing, outcome, Optional.empty(), Optional.empty(), comparablesUsados);
    }

    public static OpportunityResult encontrada(Listing listing, ProfitCalculation calculo,
                                                 GananciaClasificacion clasificacion, int comparablesUsados) {
        return new OpportunityResult(listing, AnalysisOutcome.OPPORTUNITY_FOUND,
                Optional.of(calculo), Optional.of(clasificacion), comparablesUsados);
    }
}
