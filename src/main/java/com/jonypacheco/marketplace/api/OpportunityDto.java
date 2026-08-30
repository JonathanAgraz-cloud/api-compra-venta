package com.jonypacheco.marketplace.api;

import com.jonypacheco.marketplace.persistence.domain.entity.AlertSent;
import com.jonypacheco.marketplace.persistence.domain.enums.GananciaClasificacion;
import com.jonypacheco.marketplace.persistence.domain.enums.ZonaMerida;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Representacion de una oportunidad detectada para el dashboard: combina el
 * calculo guardado en {@link AlertSent} con los datos del anuncio original en
 * {@code Listing}. Solo lectura -- no hay endpoint para crear/editar.
 */
public record OpportunityDto(
        Long id,
        String tituloAnuncio,
        String url,
        String imagenUrl,
        ZonaMerida zona,
        BigDecimal precioCompra,
        BigDecimal precioReventaEstimado,
        BigDecimal gananciaEstimada,
        GananciaClasificacion clasificacion,
        int comparablesUsados,
        boolean enviadoExitosamente,
        LocalDateTime fechaEnvio) {

    public static OpportunityDto from(AlertSent alertSent) {
        var listing = alertSent.getListing();
        return new OpportunityDto(
                alertSent.getId(),
                listing.getTitulo(),
                listing.getUrl(),
                listing.getImagenUrl(),
                listing.getZona(),
                alertSent.getPrecioCompra(),
                alertSent.getPrecioReventaEstimado(),
                alertSent.getGananciaEstimada(),
                alertSent.getClasificacion(),
                alertSent.getComparablesUsados(),
                alertSent.isEnviadoExitosamente(),
                alertSent.getFechaEnvio());
    }
}
