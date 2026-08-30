package com.jonypacheco.marketplace.persistence.domain.entity;

import com.jonypacheco.marketplace.persistence.domain.enums.ListingStatus;
import com.jonypacheco.marketplace.persistence.domain.enums.ZonaMerida;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Un anuncio crudo scrapeado de Facebook Marketplace: candidato a
 * oportunidad de reventa. No mapea relaciones inversas hacia
 * {@link ComparableListing}/{@link AlertSent} a proposito, para evitar
 * lazy-loading/N+1 innecesario; esas consultas se resuelven desde los
 * repositorios correspondientes.
 */
@Entity
@Table(name = "listings")
public class Listing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "facebook_id", nullable = false, unique = true, length = 100)
    private String facebookId;

    @Column(nullable = false, length = 300)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(length = 100)
    private String categoria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ZonaMerida zona;

    @Column(name = "ubicacion_raw", length = 255)
    private String ubicacionRaw;

    @Column(nullable = false, length = 500)
    private String url;

    @Column(name = "imagen_url", columnDefinition = "TEXT")
    private String imagenUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ListingStatus estado = ListingStatus.ACTIVE;

    @Column(name = "fecha_publicacion")
    private LocalDateTime fechaPublicacion;

    @CreationTimestamp
    @Column(name = "fecha_scrapeo", nullable = false, updatable = false)
    private LocalDateTime fechaScrapeo;

    @UpdateTimestamp
    @Column(name = "fecha_ultima_verificacion", nullable = false)
    private LocalDateTime fechaUltimaVerificacion;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected Listing() {
        // Requerido por JPA
    }

    public Listing(String facebookId, String titulo, BigDecimal precio, ZonaMerida zona, String url) {
        this.facebookId = facebookId;
        this.titulo = titulo;
        this.precio = precio;
        this.zona = zona;
        this.url = url;
    }

    public Long getId() {
        return id;
    }

    public String getFacebookId() {
        return facebookId;
    }

    public void setFacebookId(String facebookId) {
        this.facebookId = facebookId;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public ZonaMerida getZona() {
        return zona;
    }

    public void setZona(ZonaMerida zona) {
        this.zona = zona;
    }

    public String getUbicacionRaw() {
        return ubicacionRaw;
    }

    public void setUbicacionRaw(String ubicacionRaw) {
        this.ubicacionRaw = ubicacionRaw;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public ListingStatus getEstado() {
        return estado;
    }

    public void setEstado(ListingStatus estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaPublicacion() {
        return fechaPublicacion;
    }

    public void setFechaPublicacion(LocalDateTime fechaPublicacion) {
        this.fechaPublicacion = fechaPublicacion;
    }

    public LocalDateTime getFechaScrapeo() {
        return fechaScrapeo;
    }

    public LocalDateTime getFechaUltimaVerificacion() {
        return fechaUltimaVerificacion;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
