CREATE TABLE search_configs (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre         VARCHAR(150)   NOT NULL,
    categoria      VARCHAR(100)   NOT NULL,
    palabras_clave VARCHAR(500)   NOT NULL,
    zona           VARCHAR(30)    NOT NULL,
    precio_min     DECIMAL(10,2)  NULL,
    precio_max     DECIMAL(10,2)  NULL,
    activo         BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_search_configs_activo ON search_configs (activo);
CREATE INDEX idx_search_configs_zona ON search_configs (zona);
