CREATE TABLE comparables (
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    producto_normalizado  VARCHAR(200)   NOT NULL,
    categoria             VARCHAR(100)   NOT NULL,
    precio                DECIMAL(10,2)  NOT NULL,
    zona                  VARCHAR(30)    NULL,
    fuente                VARCHAR(50)    NOT NULL DEFAULT 'FACEBOOK_MARKETPLACE',
    source_listing_id     BIGINT         NULL,
    url                   VARCHAR(500)   NULL,
    fecha_observacion     TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    confiable             BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at            TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_comparables_source_listing
        FOREIGN KEY (source_listing_id) REFERENCES listings (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_comparables_producto_normalizado ON comparables (producto_normalizado);
CREATE INDEX idx_comparables_categoria ON comparables (categoria);
-- Acelera directamente la regla de negocio: minimo 5 comparables confiables por producto.
CREATE INDEX idx_comparables_producto_confiable ON comparables (producto_normalizado, confiable);
