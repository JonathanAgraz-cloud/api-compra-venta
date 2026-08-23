package com.jonypacheco.marketplace.persistence.domain.entity;

import com.jonypacheco.marketplace.persistence.domain.enums.ZonaMerida;
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
 * Un punto de referencia de precio de mercado para un producto normalizado
 * (histórico de comparables). No se llama {@code Comparable} para no chocar
 * con {@link java.lang.Comparable} del JDK. El vinculo a {@link Listing} es
 * opcional: no todo comparable tiene que venir de un anuncio scrapeado.
 */
@Entity
@Table(name = "comparables")
public class ComparableListing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "producto_normalizado", nullable = false, length = 200)
    private String productoNormalizado;

    @Column(nullable = false, length = 100)
    private String categoria;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private ZonaMerida zona;

    @Column(nullable = false, length = 50)
    private String fuente = "FACEBOOK_MARKETPLACE";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_listing_id", nullable = true)
    private Listing sourceListing;

    @Column(length = 500)
    private String url;

    @CreationTimestamp
    @Column(name = "fecha_observacion", nullable = false, updatable = false)
    private LocalDateTime fechaObservacion;

    @Column(nullable = false)
    private boolean confiable = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected ComparableListing() {
        // Requerido por JPA
    }

    public ComparableListing(String productoNormalizado, String categoria, BigDecimal precio) {
        this.productoNormalizado = productoNormalizado;
        this.categoria = categoria;
        this.precio = precio;
    }

    public Long getId() {
        return id;
    }

    public String getProductoNormalizado() {
        return productoNormalizado;
    }

    public void setProductoNormalizado(String productoNormalizado) {
        this.productoNormalizado = productoNormalizado;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public ZonaMerida getZona() {
        return zona;
    }

    public void setZona(ZonaMerida zona) {
        this.zona = zona;
    }

    public String getFuente() {
        return fuente;
    }

    public void setFuente(String fuente) {
        this.fuente = fuente;
    }

    public Listing getSourceListing() {
        return sourceListing;
    }

    public void setSourceListing(Listing sourceListing) {
        this.sourceListing = sourceListing;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public LocalDateTime getFechaObservacion() {
        return fechaObservacion;
    }

    public boolean isConfiable() {
        return confiable;
    }

    public void setConfiable(boolean confiable) {
        this.confiable = confiable;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
