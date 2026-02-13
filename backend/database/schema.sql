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
    eje_id VARCHAR(50) NOT NULL,
    nombre VARCHAR(255) NOT NULL,
    descripcion LONGTEXT,
    inicio VARCHAR(20),
    fin VARCHAR(20),
    certeza VARCHAR(50),
    dependencias JSON,
    posicion INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (roadmap_id) REFERENCES roadmaps(id) ON DELETE CASCADE,
    FOREIGN KEY (eje_id) REFERENCES ejes_estrategicos(id) ON DELETE CASCADE,
    INDEX idx_roadmap (roadmap_id),
    INDEX idx_eje (eje_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Indexes for better performance
CREATE INDEX idx_roadmaps_org ON roadmaps(organizacion);
CREATE INDEX idx_roadmaps_created ON roadmaps(created_at);
