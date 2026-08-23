package com.jonypacheco.marketplace.scraper.ingest;

/** Resultado de procesar una {@code RawListingCard} contra la base de datos. */
public enum IngestOutcome {
    NEW,
    UPDATED,
    SKIPPED_INVALID_PRICE,
    SKIPPED_INVALID_ID
}
