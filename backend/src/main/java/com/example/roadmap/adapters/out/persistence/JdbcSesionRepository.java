package com.example.roadmap.adapters.out.persistence;

import com.example.roadmap.domain.Sesion;
import com.example.roadmap.domain.SesionRepository;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación JDBC del repositorio de Sesiones.
 *
 * @since 1.0
 */
@Repository
public class JdbcSesionRepository implements SesionRepository {

    private final DbConnectionManager dbConnectionManager;

    public JdbcSesionRepository(DbConnectionManager dbConnectionManager) {
        this.dbConnectionManager = dbConnectionManager;
    }

    @Override
    public Optional<Sesion> findByToken(String token) {
        String sql = "SELECT * FROM sesiones WHERE token = ? AND estado = 'ACTIVA' AND expires_at > NOW()";
        try (Connection conn = dbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, token);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToSesion(rs));
            }
        } catch (SQLException e) {
            // Log error
        }
        return Optional.empty();
    }

    @Override
    public Optional<Sesion> findById(Integer id) {
        String sql = "SELECT * FROM sesiones WHERE id = ?";
        try (Connection conn = dbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToSesion(rs));
            }
        } catch (SQLException e) {
            // Log error
        }
        return Optional.empty();
    }

    @Override
    public List<Sesion> findActiveByUsuarioId(Integer usuarioId) {
        String sql = "SELECT * FROM sesiones WHERE usuario_id = ? AND estado = 'ACTIVA' AND expires_at > NOW()";
        List<Sesion> sesiones = new ArrayList<>();
        try (Connection conn = dbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                sesiones.add(mapResultSetToSesion(rs));
            }
        } catch (SQLException e) {
            // Log error
        }
        return sesiones;
    }

    @Override
    public Sesion save(Sesion sesion) {
        String sql = "INSERT INTO sesiones (usuario_id, token, ip_address, user_agent, expires_at, estado) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, sesion.getUsuarioId());
            ps.setString(2, sesion.getToken());
            ps.setString(3, sesion.getIpAddress());
            ps.setString(4, sesion.getUserAgent());
            ps.setTimestamp(5, java.sql.Timestamp.valueOf(sesion.getExpiresAt()));
            ps.setString(6, sesion.getEstado().name());

            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new SQLException("No se insertó la sesión");
            }
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                sesion.setId(keys.getInt(1));
                sesion.setCreatedAt(LocalDateTime.now());
            } else {
                throw new SQLException("No se devolvió ID generado para la sesión");
            }
            return sesion;
        } catch (SQLException e) {
            throw new RuntimeException("Error guardando la sesión", e);
        }
    }

    @Override
    public boolean updateEstado(Integer sesionId, Sesion.Estado estado) {
        String sql = "UPDATE sesiones SET estado = ? WHERE id = ?";
        try (Connection conn = dbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, estado.name());
            ps.setInt(2, sesionId);

            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            // Log error
            return false;
        }
    }

    @Override
    public boolean revokeByToken(String token) {
        String sql = "UPDATE sesiones SET estado = 'REVOCADA' WHERE token = ? AND estado != 'REVOCADA'";
        try (Connection conn = dbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, token);

            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            // Log error
            return false;
        }
    }

    @Override
    public int revokeAllByUsuarioId(Integer usuarioId) {
        String sql = "UPDATE sesiones SET estado = 'REVOCADA' WHERE usuario_id = ? AND estado != 'REVOCADA'";
        try (Connection conn = dbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);

            return ps.executeUpdate();
        } catch (SQLException e) {
            // Log error
            return 0;
        }
    }

    @Override
    public int deleteExpired() {
        String sql = "DELETE FROM sesiones WHERE expires_at < NOW()";
        try (Connection conn = dbConnectionManager.getConnection();
             Statement st = conn.createStatement()) {
            return st.executeUpdate(sql);
        } catch (SQLException e) {
            // Log error
            return 0;
        }
    }

    @Override
    public boolean isTokenActive(String token) {
        String sql = "SELECT COUNT(*) FROM sesiones WHERE token = ? AND estado = 'ACTIVA' AND expires_at > NOW()";
        try (Connection conn = dbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, token);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            // Log error
        }
        return false;
    }

    private Sesion mapResultSetToSesion(ResultSet rs) throws SQLException {
        Sesion sesion = new Sesion();
        sesion.setId(rs.getInt("id"));
        sesion.setUsuarioId(rs.getInt("usuario_id"));
        sesion.setToken(rs.getString("token"));
        sesion.setIpAddress(rs.getString("ip_address"));
        sesion.setUserAgent(rs.getString("user_agent"));

        java.sql.Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            sesion.setCreatedAt(createdAt.toLocalDateTime());
        }

        java.sql.Timestamp expiresAt = rs.getTimestamp("expires_at");
        if (expiresAt != null) {
            sesion.setExpiresAt(expiresAt.toLocalDateTime());
        }

        sesion.setEstado(Sesion.Estado.valueOf(rs.getString("estado")));

        return sesion;
    }
}
