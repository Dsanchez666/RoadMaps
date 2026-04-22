package com.example.roadmap.adapters.out.persistence;

import com.example.roadmap.DatabaseConnection;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Wrapper de acceso a la conexión de base de datos compartida.
 *
 * Este administrador delega en DatabaseConnection para mantener compatibilidad
 * con la implementación actual del backend.
 *
 * @since 1.0
 */
@Component
public class DbConnectionManager {

    public Connection getConnection() throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            throw new SQLException("No hay conexión activa con la base de datos.");
        }
        return conn;
    }
}

