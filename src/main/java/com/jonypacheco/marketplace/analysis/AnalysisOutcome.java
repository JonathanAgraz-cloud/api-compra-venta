package com.jonypacheco.marketplace.analysis;

/** Resultado de evaluar un {@code Listing} contra la regla de negocio de ganancia. */
public enum AnalysisOutcome {
    OPPORTUNITY_FOUND,
    LISTING_NOT_ACTIVE,
    ALREADY_ALERTED,
    MISSING_CATEGORIA,
    INSUFFICIENT_COMPARABLES,
    PROFIT_TOO_LOW
}
