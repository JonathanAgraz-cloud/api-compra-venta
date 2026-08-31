package com.jonypacheco.marketplace.alert.message;

import com.jonypacheco.marketplace.alert.AlertBatchSummary;
import com.jonypacheco.marketplace.analysis.AnalysisOutcome;
import com.jonypacheco.marketplace.analysis.OpportunityResult;
import com.jonypacheco.marketplace.analysis.pricing.ProfitCalculation;
import com.jonypacheco.marketplace.persistence.domain.entity.Listing;
import com.jonypacheco.marketplace.persistence.domain.enums.GananciaClasificacion;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Arma el texto del mensaje de Telegram para una oportunidad encontrada,
 * con el contenido que pide arquitectura-tecnica.md seccion 5: enlace del
 * anuncio, precio de compra, precio de reventa estimado, ganancia en pesos
 * y clasificacion. Texto plano (sin {@code parse_mode}): el titulo viene de
 * texto scrapeado, tratado siempre como dato no confiable, y un parseo
 * Markdown/HTML fallido tumbaria el envio completo -- Telegram igual hace
 * clickeable una URL en texto plano.
 */
public final class AlertMessageFormatter {

    private static final DecimalFormat MONTO = new DecimalFormat("#,##0.00", new DecimalFormatSymbols(Locale.US));

    private AlertMessageFormatter() {
    }

    /** Precondicion: {@code resultado.outcome() == AnalysisOutcome.OPPORTUNITY_FOUND}. */
    public static String format(OpportunityResult resultado) {
        if (resultado.outcome() != AnalysisOutcome.OPPORTUNITY_FOUND) {
            throw new IllegalArgumentException("Solo se puede formatear un OpportunityResult con outcome OPPORTUNITY_FOUND");
        }

        Listing listing = resultado.listing();
        ProfitCalculation calculo = resultado.calculo()
                .orElseThrow(() -> new IllegalStateException("OPPORTUNITY_FOUND sin calculo"));
        GananciaClasificacion clasificacion = resultado.clasificacion()
                .orElseThrow(() -> new IllegalStateException("OPPORTUNITY_FOUND sin clasificacion"));

        return emoji(clasificacion) + " Oportunidad " + clasificacion + " -- $" + MONTO.format(calculo.gananciaEstimada()) + " de ganancia estimada\n"
                + "\n"
                + listing.getTitulo() + "\n"
                + "Precio de compra: $" + monto(listing.getPrecio()) + "\n"
                + "Precio de reventa estimado: $" + MONTO.format(calculo.precioReventaEstimado()) + "\n"
                + "Zona: " + listing.getZona().name().replace('_', ' ') + "\n"
                + "Comparables usados: " + resultado.comparablesUsados() + "\n"
                + "\n"
                + listing.getUrl();
    }

    /**
     * Mensaje de "corrida completa, sin alertas nuevas" -- a peticion de Jony,
     * para saber que el scraper sigue vivo y corriendo aunque esa hora no haya
     * encontrado nada que valga la pena (ver AlertService#processOpportunities).
     */
    public static String formatSinOportunidadesNuevas(AlertBatchSummary resumen) {
        StringBuilder texto = new StringBuilder("🔍 Corrida completa, sin alertas nuevas\n\n");
        texto.append("Anuncios revisados sin oportunidad: ").append(resumen.noOportunidades()).append("\n");
        if (resumen.yaAlertadas() > 0) {
            texto.append("Oportunidades que ya se habian notificado antes: ").append(resumen.yaAlertadas()).append("\n");
        }
        if (resumen.fallidas() > 0) {
            texto.append("Fallos de envio (se reintentan solos la siguiente hora): ").append(resumen.fallidas()).append("\n");
        }
        texto.append("\nSeguimos revisando cada hora.");
        return texto.toString();
    }

    private static String monto(BigDecimal valor) {
        return MONTO.format(valor);
    }

    private static String emoji(GananciaClasificacion clasificacion) {
        return switch (clasificacion) {
            case ALTA -> "🔴";
            case MEDIA -> "🟡";
            case BAJA -> "🟢";
        };
    }
}
