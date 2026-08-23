package com.jonypacheco.marketplace.persistence.domain.entity;

import com.jonypacheco.marketplace.persistence.domain.enums.GananciaClasificacion;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Registro de que ya se avisó por Telegram sobre un listing, con el
 * calculo de ganancia usado en ese momento (snapshot para auditoria). El
 * UNIQUE en {@code listing_id} (ver V4 migration) garantiza a nivel de base
 * de datos que un mismo listing nunca genera dos alertas.
 */
@Entity
@Table(name = "alerts_sent")
public class AlertSent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "listing_id", nullable = false, unique = true)
    private Listing listing;

    @Column(name = "precio_compra", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioCompra;

    @Column(name = "precio_reventa_estimado", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioReventaEstimado;

    @Column(name = "ganancia_estimada", nullable = false, precision = 10, scale = 2)
    private BigDecimal gananciaEstimada;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private GananciaClasificacion clasificacion;

    @Column(name = "comparables_usados", nullable = false)
    private int comparablesUsados;

    @Column(name = "mensaje_telegram", columnDefinition = "TEXT")
    private String mensajeTelegram;

    @Column(name = "enviado_exitosamente", nullable = false)
    private boolean enviadoExitosamente = true;

    @CreationTimestamp
    @Column(name = "fecha_envio", nullable = false, updatable = false)
    private LocalDateTime fechaEnvio;

    protected AlertSent() {
        // Requerido por JPA
    }

    public AlertSent(Listing listing, BigDecimal precioCompra, BigDecimal precioReventaEstimado,
                      BigDecimal gananciaEstimada, GananciaClasificacion clasificacion, int comparablesUsados) {
        this.listing = listing;
        this.precioCompra = precioCompra;
        this.precioReventaEstimado = precioReventaEstimado;
        this.gananciaEstimada = gananciaEstimada;
        this.clasificacion = clasificacion;
        this.comparablesUsados = comparablesUsados;
    }

    public Long getId() {
        return id;
    }

    public Listing getListing() {
        return listing;
    }

    public void setListing(Listing listing) {
        this.listing = listing;
    }

    public BigDecimal getPrecioCompra() {
        return precioCompra;
    }

    public void setPrecioCompra(BigDecimal precioCompra) {
        this.precioCompra = precioCompra;
    }

    public BigDecimal getPrecioReventaEstimado() {
        return precioReventaEstimado;
    }

    public void setPrecioReventaEstimado(BigDecimal precioReventaEstimado) {
        this.precioReventaEstimado = precioReventaEstimado;
    }

    public BigDecimal getGananciaEstimada() {
        return gananciaEstimada;
    }

    public void setGananciaEstimada(BigDecimal gananciaEstimada) {
        this.gananciaEstimada = gananciaEstimada;
    }

    public GananciaClasificacion getClasificacion() {
        return clasificacion;
    }

    public void setClasificacion(GananciaClasificacion clasificacion) {
        this.clasificacion = clasificacion;
    }

    public int getComparablesUsados() {
        return comparablesUsados;
    }

    public void setComparablesUsados(int comparablesUsados) {
        this.comparablesUsados = comparablesUsados;
    }

    public String getMensajeTelegram() {
        return mensajeTelegram;
    }

    public void setMensajeTelegram(String mensajeTelegram) {
        this.mensajeTelegram = mensajeTelegram;
    }

    public boolean isEnviadoExitosamente() {
        return enviadoExitosamente;
    }

    public void setEnviadoExitosamente(boolean enviadoExitosamente) {
        this.enviadoExitosamente = enviadoExitosamente;
    }

    public LocalDateTime getFechaEnvio() {
        return fechaEnvio;
    }
}
