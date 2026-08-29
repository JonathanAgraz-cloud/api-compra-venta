package com.jonypacheco.marketplace.alert;

/** Resultado de procesar un {@code OpportunityResult} contra el envio de alertas. */
public enum AlertOutcome {
    SENT,
    NOT_AN_OPPORTUNITY,
    ALREADY_ALERTED,
    SEND_FAILED
}
