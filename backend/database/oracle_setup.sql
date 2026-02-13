-- Oracle Database Setup for ROADMAP MVP
-- This script creates the necessary tables for the ROADMAP application in Oracle Database

-- Connect as SYSTEM or DBA user with privileges to create tables
-- sqlplus system/password@ORCL @oracle_setup.sql

-- ============================================================
-- Create ROADMAP table
-- ============================================================
CREATE TABLE roadmap (
    id VARCHAR2(36) PRIMARY KEY,
    title VARCHAR2(255) NOT NULL,
    description VARCHAR2(4000),
    producto VARCHAR2(255),
    organizacion VARCHAR2(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR2(50) DEFAULT 'active'
);

COMMENT ON TABLE roadmap IS 'Tabla principal de roadmaps del proyecto';
COMMENT ON COLUMN roadmap.id IS 'Identificador único del roadmap (UUID)';
COMMENT ON COLUMN roadmap.title IS 'Título del roadmap';
COMMENT ON COLUMN roadmap.description IS 'Descripción detallada del roadmap';
COMMENT ON COLUMN roadmap.producto IS 'Producto asociado al roadmap';
COMMENT ON COLUMN roadmap.organizacion IS 'Organización asociada al roadmap';

-- ============================================================
-- Create HORIZONTE_BASE table
-- ============================================================
CREATE TABLE horizonte_base (
    id VARCHAR2(36) PRIMARY KEY,
    roadmap_id VARCHAR2(36) NOT NULL,
    inicio VARCHAR2(50),
    fin VARCHAR2(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (roadmap_id) REFERENCES roadmap(id) ON DELETE CASCADE
);

COMMENT ON TABLE horizonte_base IS 'Define el horizonte temporal base del roadmap';
COMMENT ON COLUMN horizonte_base.roadmap_id IS 'Referencia al roadmap padre';
COMMENT ON COLUMN horizonte_base.inicio IS 'Fecha de inicio (ej: 2026-T1 para Q1 2026)';
COMMENT ON COLUMN horizonte_base.fin IS 'Fecha de finalización';

-- ============================================================
-- Create EJES_ESTRATEGICOS table
-- ============================================================
CREATE TABLE ejes_estrategicos (
    id VARCHAR2(36) PRIMARY KEY,
    roadmap_id VARCHAR2(36) NOT NULL,
    nombre VARCHAR2(255) NOT NULL,
    descripcion VARCHAR2(4000),
    orden NUMBER(3),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (roadmap_id) REFERENCES roadmap(id) ON DELETE CASCADE
);

CREATE INDEX idx_ejes_roadmap ON ejes_estrategicos(roadmap_id);
COMMENT ON TABLE ejes_estrategicos IS 'Ejes estratégicos que componen el roadmap';
COMMENT ON COLUMN ejes_estrategicos.roadmap_id IS 'Referencia al roadmap padre';
COMMENT ON COLUMN ejes_estrategicos.nombre IS 'Nombre del eje estratégico';

-- ============================================================
-- Create INICIATIVAS table
-- ============================================================
CREATE TABLE iniciativas (
    id VARCHAR2(36) PRIMARY KEY,
    roadmap_id VARCHAR2(36) NOT NULL,
    eje_id VARCHAR2(36),
    nombre VARCHAR2(255) NOT NULL,
    descripcion VARCHAR2(4000),
    estado VARCHAR2(50) DEFAULT 'planificada',
    fecha_inicio VARCHAR2(50),
    fecha_fin VARCHAR2(50),
    prioridad VARCHAR2(50),
    owner VARCHAR2(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (roadmap_id) REFERENCES roadmap(id) ON DELETE CASCADE,
    FOREIGN KEY (eje_id) REFERENCES ejes_estrategicos(id) ON DELETE SET NULL
);

CREATE INDEX idx_iniciativas_roadmap ON iniciativas(roadmap_id);
CREATE INDEX idx_iniciativas_eje ON iniciativas(eje_id);
COMMENT ON TABLE iniciativas IS 'Iniciativas concretas del roadmap';
COMMENT ON COLUMN iniciativas.estado IS 'Estado de la iniciativa: planificada, en_progreso, completada, bloqueada';
COMMENT ON COLUMN iniciativas.prioridad IS 'Prioridad: baja, media, alta, crítica';

-- ============================================================
-- Create HITOS table (Milestones)
-- ============================================================
CREATE TABLE hitos (
    id VARCHAR2(36) PRIMARY KEY,
    iniciativa_id VARCHAR2(36) NOT NULL,
    nombre VARCHAR2(255) NOT NULL,
    fechaObjetivo VARCHAR2(50),
    estado VARCHAR2(50) DEFAULT 'pendiente',
    descripcion VARCHAR2(4000),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (iniciativa_id) REFERENCES iniciativas(id) ON DELETE CASCADE
);

CREATE INDEX idx_hitos_iniciativa ON hitos(iniciativa_id);
COMMENT ON TABLE hitos IS 'Hitos o milestones de las iniciativas';

-- ============================================================
-- Create USUARIOS table
-- ============================================================
CREATE TABLE usuarios (
    id VARCHAR2(36) PRIMARY KEY,
    nombre VARCHAR2(255) NOT NULL,
    email VARCHAR2(255) UNIQUE,
    rol VARCHAR2(50),
    estado VARCHAR2(50) DEFAULT 'activo',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE usuarios IS 'Usuarios del sistema de roadmap';

-- ============================================================
-- Create COMENTARIOS table
-- ============================================================
CREATE TABLE comentarios (
    id VARCHAR2(36) PRIMARY KEY,
    roadmap_id VARCHAR2(36),
    iniciativa_id VARCHAR2(36),
    usuario_id VARCHAR2(36),
    contenido VARCHAR2(4000) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (roadmap_id) REFERENCES roadmap(id) ON DELETE CASCADE,
    FOREIGN KEY (iniciativa_id) REFERENCES iniciativas(id) ON DELETE CASCADE,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE SET NULL
);

CREATE INDEX idx_comentarios_roadmap ON comentarios(roadmap_id);
CREATE INDEX idx_comentarios_iniciativa ON comentarios(iniciativa_id);
COMMENT ON TABLE comentarios IS 'Comentarios en roadmaps e iniciativas';

-- ============================================================
-- Create sequences for ID generation (optional)
-- ============================================================
CREATE SEQUENCE seq_roadmap START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_eje INCREMENT BY 1;
CREATE SEQUENCE seq_iniciativa INCREMENT BY 1;
CREATE SEQUENCE seq_hito INCREMENT BY 1;
CREATE SEQUENCE seq_usuario INCREMENT BY 1;
CREATE SEQUENCE seq_comentario INCREMENT BY 1;

-- ============================================================
-- Create views for common queries
-- ============================================================

-- Vista: Resumen de roadmaps con cantidad de iniciativas
CREATE OR REPLACE VIEW v_roadmaps_summary AS
SELECT 
    r.id,
    r.title,
    r.descripcion,
    r.organizacion,
    r.created_at,
    COUNT(DISTINCT i.id) as total_iniciativas,
    COUNT(DISTINCT e.id) as total_ejes
FROM roadmap r
LEFT JOIN iniciativas i ON r.id = i.roadmap_id
LEFT JOIN ejes_estrategicos e ON r.id = e.roadmap_id
GROUP BY r.id, r.title, r.descripcion, r.organizacion, r.created_at;

-- Vista: Iniciativas por estado
CREATE OR REPLACE VIEW v_iniciativas_por_estado AS
SELECT 
    roadmap_id,
    estado,
    COUNT(*) as total
FROM iniciativas
GROUP BY roadmap_id, estado;

-- Vista: Roadmaps activos
CREATE OR REPLACE VIEW v_roadmaps_activos AS
SELECT *
FROM roadmap
WHERE status = 'active'
ORDER BY created_at DESC;

-- ============================================================
-- Create stored procedures
-- ============================================================

-- Procedure: Crear un nuevo roadmap completo
CREATE OR REPLACE PROCEDURE sp_crear_roadmap (
    p_id VARCHAR2,
    p_title VARCHAR2,
    p_description VARCHAR2,
    p_producto VARCHAR2,
    p_organizacion VARCHAR2
) AS
BEGIN
    INSERT INTO roadmap (id, title, description, producto, organizacion)
    VALUES (p_id, p_title, p_description, p_producto, p_organizacion);
    COMMIT;
    DBMS_OUTPUT.PUT_LINE('Roadmap creado exitosamente: ' || p_id);
EXCEPTION
    WHEN DUP_VAL_ON_INDEX THEN
        DBMS_OUTPUT.PUT_LINE('ERROR: El roadmap ya existe');
        ROLLBACK;
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('ERROR: ' || SQLERRM);
        ROLLBACK;
END sp_crear_roadmap;
/

-- Procedure: Obtener todos los roadmaps
CREATE OR REPLACE PROCEDURE sp_obtener_roadmaps (
    p_cursor OUT SYS_REFCURSOR
) AS
BEGIN
    OPEN p_cursor FOR
    SELECT * FROM v_roadmaps_activos;
END sp_obtener_roadmaps;
/

-- ============================================================
-- Grant permissions (if needed)
-- ============================================================
-- GRANT SELECT, INSERT, UPDATE, DELETE ON roadmap TO app_user;
-- GRANT SELECT, INSERT, UPDATE, DELETE ON iniciativas TO app_user;
-- GRANT SELECT, INSERT, UPDATE, DELETE ON ejes_estrategicos TO app_user;
-- GRANT SELECT ON v_roadmaps_summary TO app_user;
-- GRANT EXECUTE ON sp_crear_roadmap TO app_user;

-- ============================================================
-- Insert sample data (optional)
-- ============================================================

INSERT INTO roadmap (id, title, description, producto, organizacion)
VALUES (
    '550e8400-e29b-41d4-a716-446655440000',
    'Roadmap ETNA 2026',
    'Roadmap estratégico del producto ETNA para el año 2026',
    'ETNA',
    'AENA'
);

INSERT INTO ejes_estrategicos (id, roadmap_id, nombre, descripcion, orden)
VALUES (
    '550e8400-e29b-41d4-a716-446655440001',
    '550e8400-e29b-41d4-a716-446655440000',
    'Optimización de Operaciones',
    'Mejora continua de los procesos operacionales',
    1
);

INSERT INTO iniciativas (id, roadmap_id, eje_id, nombre, descripcion, estado, prioridad)
VALUES (
    '550e8400-e29b-41d4-a716-446655440002',
    '550e8400-e29b-41d4-a716-446655440000',
    '550e8400-e29b-41d4-a716-446655440001',
    'Digitalización de Procesos',
    'Convertir procesos manuales a automáticos',
    'en_progreso',
    'alta'
);

COMMIT;

-- ============================================================
-- Verify installation
-- ============================================================
SELECT table_name FROM user_tables 
WHERE table_name IN ('ROADMAP', 'INICIATIVAS', 'EJES_ESTRATEGICOS', 'HITOS', 'USUARIOS', 'COMENTARIOS')
ORDER BY table_name;

PROMPT
PROMPT ========================================
PROMPT  Oracle Database Setup Completed!
PROMPT ========================================
PROMPT
PROMPT Tables created:
PROMPT   - ROADMAP
PROMPT   - HORIZONTE_BASE
PROMPT   - EJES_ESTRATEGICOS
PROMPT   - INICIATIVAS
PROMPT   - HITOS
PROMPT   - USUARIOS
PROMPT   - COMENTARIOS
PROMPT
PROMPT Views created:
PROMPT   - V_ROADMAPS_SUMMARY
PROMPT   - V_INICIATIVAS_POR_ESTADO
PROMPT   - V_ROADMAPS_ACTIVOS
PROMPT
PROMPT Procedures created:
PROMPT   - SP_CREAR_ROADMAP
PROMPT   - SP_OBTENER_ROADMAPS
PROMPT
PROMPT Sequences created for ID generation
PROMPT
PROMPT Sample data inserted (optional)
PROMPT
PROMPT ========================================
