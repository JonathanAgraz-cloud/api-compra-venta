package com.jonypacheco.marketplace.alert;

import com.jonypacheco.marketplace.alert.message.AlertMessageFormatter;
import com.jonypacheco.marketplace.alert.telegram.TelegramClient;
import com.jonypacheco.marketplace.analysis.AnalysisOutcome;
import com.jonypacheco.marketplace.analysis.OpportunityResult;
import com.jonypacheco.marketplace.analysis.pricing.ProfitCalculation;
import com.jonypacheco.marketplace.persistence.domain.entity.AlertSent;
import com.jonypacheco.marketplace.persistence.domain.entity.Listing;
import com.jonypacheco.marketplace.persistence.domain.enums.GananciaClasificacion;
import com.jonypacheco.marketplace.persistence.repository.AlertSentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Orquestador del modulo de alertas: para cada {@link OpportunityResult}
 * encontrado (ver {@code analysis}), envia el mensaje por Telegram y
 * registra el envio en {@code alerts_sent}. No tiene {@code @Scheduled}; un
 * futuro scheduler llamara a {@link #processOpportunities} con el resultado
 * de {@code OpportunityAnalysisService.analyzeAllActiveListings()}.
 * <p>
 * Regla de negocio confirmada: si falla el envio a Telegram, NO se persiste
 * {@link AlertSent} -- el dedupe (`existsByListing_Id`) no bloquea, y la
 * misma oportunidad se reintenta sola en la siguiente corrida del
 * scheduler, sin logica de reintento adicional.
 */
@Service
public class AlertService {

    private static final Logger log = LoggerFactory.getLogger(AlertService.class);

    private final AlertSentRepository alertSentRepository;
    private final TelegramClient telegramClient;

    public AlertService(AlertSentRepository alertSentRepository, TelegramClient telegramClient) {
        this.alertSentRepository = alertSentRepository;
        this.telegramClient = telegramClient;
    }

    public AlertBatchSummary processOpportunities(List<OpportunityResult> resultados) {
        AlertBatchSummary resumen = AlertBatchSummary.vacio();
        for (OpportunityResult resultado : resultados) {
            resumen = resumen.sumar(sendAlert(resultado));
        }
        log.info("Procesamiento de alertas completo: {}", resumen);

        if (resumen.enviadas() == 0) {
            notifySinOportunidadesNuevas(resumen);
        }
        return resumen;
    }

    /**
     * Aviso de "corrida completa, sin nada nuevo" pedido por Jony para estar
     * pendiente de que el scraper sigue corriendo aunque esa hora no haya
     * encontrado ninguna oportunidad nueva que alertar. No es critico: si
     * falla el envio, solo se loggea (no bloquea ni reintenta, a diferencia
     * de una alerta real de oportunidad).
     */
    private void notifySinOportunidadesNuevas(AlertBatchSummary resumen) {
        String mensaje = AlertMessageFormatter.formatSinOportunidadesNuevas(resumen);
        boolean enviado = telegramClient.sendMessage(mensaje);
        if (!enviado) {
            log.warn("No se pudo enviar el aviso de 'sin alertas nuevas' a Telegram (no es critico, se omite)");
        }
    }

    public AlertOutcome sendAlert(OpportunityResult resultado) {
        if (resultado.outcome() != AnalysisOutcome.OPPORTUNITY_FOUND) {
            return AlertOutcome.NOT_AN_OPPORTUNITY;
        }

        Listing listing = resultado.listing();
        if (alertSentRepository.existsByListing_Id(listing.getId())) {
            return AlertOutcome.ALREADY_ALERTED;
        }

        String mensaje = AlertMessageFormatter.format(resultado);
        boolean enviado = telegramClient.sendMessage(mensaje);
        if (!enviado) {
            log.error("No se pudo enviar la alerta de Telegram para el listing {}, se reintentara en la siguiente corrida",
                    listing.getFacebookId());
            return AlertOutcome.SEND_FAILED;
        }

        ProfitCalculation calculo = resultado.calculo().orElseThrow();
        GananciaClasificacion clasificacion = resultado.clasificacion().orElseThrow();
        AlertSent alerta = new AlertSent(listing, listing.getPrecio(), calculo.precioReventaEstimado(),
                calculo.gananciaEstimada(), clasificacion, resultado.comparablesUsados());
        alerta.setMensajeTelegram(mensaje);
        alerta.setEnviadoExitosamente(true);

        try {
            alertSentRepository.save(alerta);
        } catch (DataIntegrityViolationException e) {
            log.warn("El listing {} ya tenia una alerta registrada (condicion de carrera), se omite el duplicado",
                    listing.getFacebookId());
            return AlertOutcome.ALREADY_ALERTED;
        }

        log.info("Alerta enviada para el listing {}", listing.getFacebookId());
        return AlertOutcome.SENT;
    }
}
