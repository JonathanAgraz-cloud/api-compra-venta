CREATE TABLE listings (
    id                         BIGINT AUTO_INCREMENT PRIMARY KEY,
    facebook_id                VARCHAR(100)   NOT NULL,
    titulo                     VARCHAR(300)   NOT NULL,
    descripcion                TEXT           NULL,
    precio                     DECIMAL(10,2)  NOT NULL,
    categoria                  VARCHAR(100)   NULL,
    zona                       VARCHAR(30)    NOT NULL,
    ubicacion_raw              VARCHAR(255)   NULL,
    url                        VARCHAR(500)   NOT NULL,
    imagen_url                 VARCHAR(500)   NULL,
    estado                     VARCHAR(20)    NOT NULL DEFAULT 'ACTIVE',
    fecha_publicacion          TIMESTAMP      NULL,
    fecha_scrapeo              TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_ultima_verificacion  TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_at                 TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                 TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_listings_facebook_id UNIQUE (facebook_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_listings_zona ON listings (zona);
CREATE INDEX idx_listings_estado ON listings (estado);
CREATE INDEX idx_listings_categoria ON listings (categoria);
CREATE INDEX idx_listings_precio ON listings (precio);
