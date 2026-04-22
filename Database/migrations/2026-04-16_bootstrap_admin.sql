-- Migración: Bootstrap - Crear usuario inicial ADMIN
-- Fecha: 2026-04-16
-- Descripción: Inserta usuario admin inicial sin contraseña (debe crearla en primer login)

INSERT INTO usuarios (username, password_hash, rol, activo, must_change_password, created_by, updated_by)
VALUES ('admin', NULL, 'ADMIN', TRUE, TRUE, 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    rol = 'ADMIN',
    activo = TRUE,
    must_change_password = TRUE,
    updated_by = 'SYSTEM';
