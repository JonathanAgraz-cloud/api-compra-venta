package com.jonypacheco.marketplace.api;

import com.jonypacheco.marketplace.persistence.repository.AlertSentRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoint de solo lectura para el dashboard: expone las oportunidades ya
 * detectadas (una fila en {@code alerts_sent} por cada una), ordenadas por
 * ganancia estimada descendente para que lo mas importante aparezca primero.
 * Protegido por HTTP Basic (ver {@code SecurityConfig}) -- no expone nada
 * publicamente.
 */
@RestController
@RequestMapping("/api/opportunities")
public class OpportunityController {

    private final AlertSentRepository alertSentRepository;

    public OpportunityController(AlertSentRepository alertSentRepository) {
        this.alertSentRepository = alertSentRepository;
    }

    @GetMapping
    public List<OpportunityDto> listarOportunidades() {
        return alertSentRepository.findAllByOrderByGananciaEstimadaDesc().stream()
                .map(OpportunityDto::from)
                .toList();
    }
}
