-- Migration: normalized expedientes model
-- Date: 2026-04-08
-- Goal:
--   1) Create reusable expedientes table.
--   2) Create iniciativa_expediente linking table.
--   3) Keep legacy iniciativas.expedientes column untouched for compatibility.

CREATE TABLE IF NOT EXISTS expedientes (
    id VARCHAR(50) PRIMARY KEY,
    tipo VARCHAR(255),
    empresa VARCHAR(255),
    expediente VARCHAR(255),
    impacto VARCHAR(255),
    precio_licitacion VARCHAR(255),
    precio_adjudicacion VARCHAR(255),
    fecha_fin_expediente VARCHAR(20),
    informacion_adicional JSON,
    huella_negocio VARCHAR(512),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_expedientes_huella (huella_negocio),
    INDEX idx_expedientes_codigo (expediente)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS iniciativa_expediente (
    iniciativa_id VARCHAR(50) NOT NULL,
    expediente_id VARCHAR(50) NOT NULL,
    posicion INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (iniciativa_id, expediente_id),
    FOREIGN KEY (iniciativa_id) REFERENCES iniciativas(id) ON DELETE CASCADE,
    FOREIGN KEY (expediente_id) REFERENCES expedientes(id) ON DELETE CASCADE,
    INDEX idx_iniciativa_expediente_expediente (expediente_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Validation query: should return both tables.
SELECT TABLE_NAME
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME IN ('expedientes', 'iniciativa_expediente');
