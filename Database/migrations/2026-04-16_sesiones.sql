-- Migración: Crear tabla sesiones
-- Fecha: 2026-04-16
-- Descripción: Tabla para almacenar tokens de sesión activos

CREATE TABLE IF NOT EXISTS sesiones (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID único de la sesión',
    usuario_id INT NOT NULL COMMENT 'FK a usuarios.id',
    token VARCHAR(500) NOT NULL UNIQUE COMMENT 'JWT token único',
    ip_address VARCHAR(45) COMMENT 'IP de origen de la sesión (IPv4 o IPv6)',
    user_agent VARCHAR(500) COMMENT 'User-Agent del cliente',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Fecha de creación de la sesión',
    expires_at TIMESTAMP NOT NULL COMMENT 'Fecha de expiración del token (8 horas por defecto)',
    estado ENUM('ACTIVA', 'EXPIRADA', 'REVOCADA') NOT NULL DEFAULT 'ACTIVA' COMMENT 'Estado de la sesión',
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    INDEX idx_usuario_id (usuario_id),
    INDEX idx_token (token),
    INDEX idx_estado (estado),
    INDEX idx_expires_at (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Almacena tokens de sesión activos para autenticación JWT';
