-- Database schema for Roadmap MVP with MySQL
-- Create database if not exists
CREATE DATABASE IF NOT EXISTS roadmap_mvp;
USE roadmap_mvp;

-- Table for Roadmaps
CREATE TABLE IF NOT EXISTS roadmaps (
    id VARCHAR(50) PRIMARY KEY,
    titulo VARCHAR(255) NOT NULL,
    descripcion LONGTEXT,
    producto VARCHAR(255),
    organizacion VARCHAR(255),
    horizonte_inicio VARCHAR(20),
    horizonte_fin VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table for Strategic Axes (Ejes Estratégicos)
CREATE TABLE IF NOT EXISTS ejes_estrategicos (
    id VARCHAR(50) PRIMARY KEY,
    roadmap_id VARCHAR(50) NOT NULL,
    nombre VARCHAR(255) NOT NULL,
    descripcion LONGTEXT,
    color VARCHAR(7),
    posicion INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (roadmap_id) REFERENCES roadmaps(id) ON DELETE CASCADE,
    INDEX idx_roadmap (roadmap_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table for Initiatives (Iniciativas)
CREATE TABLE IF NOT EXISTS iniciativas (
    id VARCHAR(50) PRIMARY KEY,
    roadmap_id VARCHAR(50) NOT NULL,
    eje_id VARCHAR(50) NULL,
    nombre VARCHAR(255) NOT NULL,
    inicio VARCHAR(20),
    fin VARCHAR(20),
    certeza VARCHAR(50),
    dependencias JSON,
    informacion_adicional JSON,
    expedientes JSON,
    posicion INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (roadmap_id) REFERENCES roadmaps(id) ON DELETE CASCADE,
    FOREIGN KEY (eje_id) REFERENCES ejes_estrategicos(id) ON DELETE SET NULL,
    INDEX idx_roadmap (roadmap_id),
    INDEX idx_eje (eje_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table for Commitments (Compromisos)
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
    INDEX idx_roadmap (roadmap_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table for Expedientes reusable across initiatives
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

-- Linking table between initiatives and expedientes
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

-- Indexes for better performance
CREATE INDEX idx_roadmaps_org ON roadmaps(organizacion);
CREATE INDEX idx_roadmaps_created ON roadmaps(created_at);
