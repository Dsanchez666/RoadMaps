-- Migration: roadmap commitments table
-- Date: 2026-03-26
-- Goal:
--   1) Ensure table compromisos exists.
--   2) Ensure required columns for dynamic additional fields.

CREATE TABLE IF NOT EXISTS compromisos (
    id VARCHAR(50) PRIMARY KEY,
    roadmap_id VARCHAR(50) NOT NULL,
    descripcion LONGTEXT,
    fecha_comprometido VARCHAR(20),
    actor VARCHAR(255),
    quien_compromete VARCHAR(255),
    informacion_adicional JSON,
    posicion INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (roadmap_id) REFERENCES roadmaps(id) ON DELETE CASCADE,
    INDEX idx_compromisos_roadmap (roadmap_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Validation query: should return zero rows.
SELECT id
FROM compromisos
WHERE informacion_adicional IS NOT NULL
  AND JSON_VALID(informacion_adicional) = 0;
