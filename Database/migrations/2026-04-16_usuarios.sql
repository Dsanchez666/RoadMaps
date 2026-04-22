-- Migración: Crear tabla usuarios
-- Fecha: 2026-04-16
-- Descripción: Tabla para almacenar usuarios del sistema con credenciales y roles

CREATE TABLE IF NOT EXISTS usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID único del usuario',
    username VARCHAR(100) NOT NULL UNIQUE COMMENT 'Username único para login',
    password_hash VARCHAR(255) COMMENT 'Hash BCrypt de la contraseña (NULL si no ha establecido contraseña)',
    rol ENUM('ADMIN', 'GESTION', 'CONSULTA') NOT NULL DEFAULT 'CONSULTA' COMMENT 'Rol del usuario',
    activo BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Usuario activo (TRUE) o deshabilitado (FALSE)',
    must_change_password BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Obliga a cambiar contraseña en primer login',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Fecha de creación del usuario',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Fecha de última actualización',
    created_by VARCHAR(100) COMMENT 'Usuario que creó este registro',
    updated_by VARCHAR(100) COMMENT 'Usuario que actualizó este registro',
    INDEX idx_username (username),
    INDEX idx_rol (rol),
    INDEX idx_activo (activo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Almacena información de usuarios y credenciales del sistema';
