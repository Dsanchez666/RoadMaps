package com.example.roadmap.adapters.out.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

/**
 * Ensures required MySQL schema objects for roadmap persistence exist.
 *
 * This initializer is idempotent and can be executed on every successful
 * MySQL connection.
 *
 * @since 1.0
 */
public final class MySqlSchemaInitializer {
    private MySqlSchemaInitializer() {
    }

    /**
     * Creates or updates roadmap tables required by the backend.
     *
     * @param connection Active MySQL connection.
     */
    public static String ensureSchema(Connection connection) {
        try (Statement st = connection.createStatement()) {
            st.execute("""
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
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                """);

            st.execute("""
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
                    INDEX idx_ejes_roadmap (roadmap_id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                """);

            st.execute("""
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
                    INDEX idx_iniciativas_roadmap (roadmap_id),
                    INDEX idx_iniciativas_eje (eje_id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                """);

            st.execute("""
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
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                """);

            st.execute("""
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
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS iniciativa_expediente (
                    iniciativa_id VARCHAR(50) NOT NULL,
                    expediente_id VARCHAR(50) NOT NULL,
                    posicion INT DEFAULT 0,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    PRIMARY KEY (iniciativa_id, expediente_id),
                    FOREIGN KEY (iniciativa_id) REFERENCES iniciativas(id) ON DELETE CASCADE,
                    FOREIGN KEY (expediente_id) REFERENCES expedientes(id) ON DELETE CASCADE,
                    INDEX idx_iniciativa_expediente_expediente (expediente_id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                """);

            // Tablas de autenticación y autorización
            st.execute("""
                CREATE TABLE IF NOT EXISTS usuarios (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    username VARCHAR(100) NOT NULL UNIQUE,
                    password_hash VARCHAR(255),
                    rol ENUM('ADMIN', 'GESTION', 'CONSULTA') NOT NULL DEFAULT 'CONSULTA',
                    activo BOOLEAN NOT NULL DEFAULT TRUE,
                    must_change_password BOOLEAN NOT NULL DEFAULT TRUE,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    created_by VARCHAR(100),
                    updated_by VARCHAR(100),
                    INDEX idx_username (username),
                    INDEX idx_rol (rol),
                    INDEX idx_activo (activo)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS sesiones (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    usuario_id INT NOT NULL,
                    token VARCHAR(500) NOT NULL UNIQUE,
                    ip_address VARCHAR(45),
                    user_agent VARCHAR(500),
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    expires_at TIMESTAMP NOT NULL,
                    estado ENUM('ACTIVA', 'EXPIRADA', 'REVOCADA') NOT NULL DEFAULT 'ACTIVA',
                    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
                    INDEX idx_usuario_id (usuario_id),
                    INDEX idx_token (token),
                    INDEX idx_estado (estado),
                    INDEX idx_expires_at (expires_at)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                """);

            // Bootstrap usuario admin inicial
            st.execute("""
                INSERT INTO usuarios (username, password_hash, rol, activo, must_change_password, created_by, updated_by)
                VALUES ('admin', NULL, 'ADMIN', TRUE, TRUE, 'SYSTEM', 'SYSTEM')
                ON DUPLICATE KEY UPDATE
                    rol = 'ADMIN',
                    activo = TRUE,
                    must_change_password = TRUE,
                    updated_by = 'SYSTEM'
                """);

            Set<String> missingIniciativas = findMissingColumns(
                connection,
                "iniciativas",
                Set.of("id", "roadmap_id", "eje_id", "nombre", "inicio", "fin", "certeza", "dependencias", "informacion_adicional", "expedientes", "posicion")
            );
            if (!missingIniciativas.isEmpty()) {
                return "Esquema de iniciativas desactualizado. Faltan columnas: "
                    + String.join(", ", missingIniciativas)
                    + ". Ejecuta migración SQL en Database/migrations antes de continuar.";
            }

            Set<String> missingCompromisos = findMissingColumns(
                connection,
                "compromisos",
                Set.of("id", "roadmap_id", "descripcion", "fecha_comprometido", "actor", "quien_compromete", "informacion_adicional", "posicion")
            );
            if (!missingCompromisos.isEmpty()) {
                return "Esquema de compromisos desactualizado. Faltan columnas: "
                    + String.join(", ", missingCompromisos)
                    + ". Ejecuta migración SQL en Database/migrations antes de continuar.";
            }

            Set<String> missingExpedientes = findMissingColumns(
                connection,
                "expedientes",
                Set.of("id", "tipo", "empresa", "expediente", "impacto", "precio_licitacion", "precio_adjudicacion",
                    "fecha_fin_expediente", "informacion_adicional", "huella_negocio")
            );
            if (!missingExpedientes.isEmpty()) {
                return "Esquema de expedientes desactualizado. Faltan columnas: "
                    + String.join(", ", missingExpedientes)
                    + ". Ejecuta migración SQL en Database/migrations antes de continuar.";
            }

            Set<String> missingIniciativaExpediente = findMissingColumns(
                connection,
                "iniciativa_expediente",
                Set.of("iniciativa_id", "expediente_id", "posicion")
            );
            if (!missingIniciativaExpediente.isEmpty()) {
                return "Esquema de enlaces iniciativa-expediente desactualizado. Faltan columnas: "
                    + String.join(", ", missingIniciativaExpediente)
                    + ". Ejecuta migración SQL en Database/migrations antes de continuar.";
            }

            // Validar esquema de autenticación y autorización
            Set<String> missingUsuarios = findMissingColumns(
                connection,
                "usuarios",
                Set.of("id", "username", "password_hash", "rol", "activo", "must_change_password", "created_by", "updated_by")
            );
            if (!missingUsuarios.isEmpty()) {
                return "Esquema de usuarios desactualizado. Faltan columnas: "
                    + String.join(", ", missingUsuarios)
                    + ". Ejecuta migración SQL en Database/migrations antes de continuar.";
            }

            Set<String> missingSesiones = findMissingColumns(
                connection,
                "sesiones",
                Set.of("id", "usuario_id", "token", "ip_address", "user_agent", "created_at", "expires_at", "estado")
            );
            if (!missingSesiones.isEmpty()) {
                return "Esquema de sesiones desactualizado. Faltan columnas: "
                    + String.join(", ", missingSesiones)
                    + ". Ejecuta migración SQL en Database/migrations antes de continuar.";
            }
            return null;
        } catch (Exception e) {
            return "No se pudo verificar esquema MySQL: " + e.getMessage();
        }
    }

    /**
     * Returns missing expected columns for one table in current database schema.
     *
     * @param connection Active MySQL connection.
     * @param tableName Table to inspect.
     * @param expectedColumns Required column names.
     * @return Set<String> missing columns.
     */
    private static Set<String> findMissingColumns(
        Connection connection,
        String tableName,
        Set<String> expectedColumns
    ) {
        Set<String> existing = new HashSet<>();
        String sql = """
            SELECT COLUMN_NAME
            FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = ?
            """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, tableName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    existing.add(rs.getString("COLUMN_NAME"));
                }
            }
        } catch (Exception e) {
            return new HashSet<>(expectedColumns);
        }

        Set<String> missing = new HashSet<>(expectedColumns);
        missing.removeAll(existing);
        return missing;
    }
}
