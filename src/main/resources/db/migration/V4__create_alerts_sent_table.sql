CREATE TABLE alerts_sent (
    id                        BIGINT AUTO_INCREMENT PRIMARY KEY,
    listing_id                BIGINT         NOT NULL,
    precio_compra             DECIMAL(10,2)  NOT NULL,
    precio_reventa_estimado   DECIMAL(10,2)  NOT NULL,
    ganancia_estimada         DECIMAL(10,2)  NOT NULL,
    clasificacion             VARCHAR(10)    NOT NULL,
    comparables_usados        INT            NOT NULL,
    mensaje_telegram          TEXT           NULL,
    enviado_exitosamente      BOOLEAN        NOT NULL DEFAULT TRUE,
    fecha_envio               TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    -- El UNIQUE convierte la regla "no duplicar alertas por listing" en una
    -- garantia de base de datos, no solo logica de aplicacion.
    CONSTRAINT uk_alerts_sent_listing_id UNIQUE (listing_id),
    CONSTRAINT fk_alerts_sent_listing
        FOREIGN KEY (listing_id) REFERENCES listings (id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_alerts_sent_clasificacion ON alerts_sent (clasificacion);
CREATE INDEX idx_alerts_sent_fecha_envio ON alerts_sent (fecha_envio);
