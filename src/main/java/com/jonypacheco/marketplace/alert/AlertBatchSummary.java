package com.jonypacheco.marketplace.alert;

/** Resumen de {@link AlertService#processOpportunities}, un contador por {@link AlertOutcome}. */
public record AlertBatchSummary(int enviadas, int yaAlertadas, int fallidas, int noOportunidades) {

    static AlertBatchSummary vacio() {
        return new AlertBatchSummary(0, 0, 0, 0);
    }

    AlertBatchSummary sumar(AlertOutcome outcome) {
        return switch (outcome) {
            case SENT -> new AlertBatchSummary(enviadas + 1, yaAlertadas, fallidas, noOportunidades);
            case ALREADY_ALERTED -> new AlertBatchSummary(enviadas, yaAlertadas + 1, fallidas, noOportunidades);
            case SEND_FAILED -> new AlertBatchSummary(enviadas, yaAlertadas, fallidas + 1, noOportunidades);
            case NOT_AN_OPPORTUNITY -> new AlertBatchSummary(enviadas, yaAlertadas, fallidas, noOportunidades + 1);
        };
    }
}
