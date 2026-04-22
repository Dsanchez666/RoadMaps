package com.example.roadmap.adapters.out.persistence;

import com.example.roadmap.domain.Usuario;
import com.example.roadmap.domain.UsuarioRepository;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación JDBC del repositorio de Usuarios.
 *
 * @since 1.0
 */
@Repository
public class JdbcUsuarioRepository implements UsuarioRepository {

    private final DbConnectionManager dbConnectionManager;

    public JdbcUsuarioRepository(DbConnectionManager dbConnectionManager) {
        this.dbConnectionManager = dbConnectionManager;
    }

    @Override
    public Optional<Usuario> findByUsername(String username) {
        String sql = "SELECT * FROM usuarios WHERE username = ?";
        try (Connection conn = dbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToUsuario(rs));
            }
        } catch (SQLException e) {
            // Log error
        }
        return Optional.empty();
    }

    @Override
    public Optional<Usuario> findById(Integer id) {
        String sql = "SELECT * FROM usuarios WHERE id = ?";
        try (Connection conn = dbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToUsuario(rs));
            }
        } catch (SQLException e) {
            // Log error
        }
        return Optional.empty();
    }

    @Override
    public List<Usuario> findAll() {
        String sql = "SELECT * FROM usuarios ORDER BY username ASC";
        List<Usuario> usuarios = new ArrayList<>();
        try (Connection conn = dbConnectionManager.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                usuarios.add(mapResultSetToUsuario(rs));
            }
        } catch (SQLException e) {
            // Log error
        }
        return usuarios;
    }

    @Override
    public Usuario save(Usuario usuario) {
        if (usuario.getId() == null) {
            return insert(usuario);
        } else {
            return update(usuario);
        }
    }

    private Usuario insert(Usuario usuario) {
        String sql = "INSERT INTO usuarios (username, password_hash, rol, activo, must_change_password, created_by, updated_by) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, usuario.getUsername());
            ps.setString(2, usuario.getPasswordHash());
            ps.setString(3, usuario.getRol().name());
            ps.setBoolean(4, usuario.getActivo() != null ? usuario.getActivo() : true);
            ps.setBoolean(5, usuario.getMustChangePassword() != null ? usuario.getMustChangePassword() : true);
            ps.setString(6, usuario.getCreatedBy() != null ? usuario.getCreatedBy() : "SYSTEM");
            ps.setString(7, usuario.getUpdatedBy() != null ? usuario.getUpdatedBy() : "SYSTEM");

            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                usuario.setId(keys.getInt(1));
                usuario.setCreatedAt(LocalDateTime.now());
                usuario.setUpdatedAt(LocalDateTime.now());
            }
        } catch (SQLException e) {
            // Log error
        }
        return usuario;
    }

    private Usuario update(Usuario usuario) {
        String sql = "UPDATE usuarios SET password_hash = ?, rol = ?, activo = ?, must_change_password = ?, updated_by = ? " +
                     "WHERE id = ?";
        try (Connection conn = dbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usuario.getPasswordHash());
            ps.setString(2, usuario.getRol().name());
            ps.setBoolean(3, usuario.getActivo() != null ? usuario.getActivo() : true);
            ps.setBoolean(4, usuario.getMustChangePassword() != null ? usuario.getMustChangePassword() : true);
            ps.setString(5, usuario.getUpdatedBy() != null ? usuario.getUpdatedBy() : "SYSTEM");
            ps.setInt(6, usuario.getId());

            ps.executeUpdate();
            usuario.setUpdatedAt(LocalDateTime.now());
        } catch (SQLException e) {
            // Log error
        }
        return usuario;
    }

    @Override
    public boolean updatePassword(Integer usuarioId, String passwordHash) {
        String sql = "UPDATE usuarios SET password_hash = ?, must_change_password = FALSE, updated_by = ? WHERE id = ?";
        try (Connection conn = dbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, passwordHash);
            ps.setString(2, "SYSTEM");
            ps.setInt(3, usuarioId);

            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            // Log error
            return false;
        }
    }

    @Override
    public boolean updateRol(Integer usuarioId, Usuario.Role rol) {
        String sql = "UPDATE usuarios SET rol = ?, updated_by = ? WHERE id = ?";
        try (Connection conn = dbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rol.name());
            ps.setString(2, "ADMIN");
            ps.setInt(3, usuarioId);

            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            // Log error
            return false;
        }
    }

    @Override
    public boolean updateActivo(Integer usuarioId, Boolean activo) {
        String sql = "UPDATE usuarios SET activo = ?, updated_by = ? WHERE id = ?";
        try (Connection conn = dbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, activo != null ? activo : true);
            ps.setString(2, "ADMIN");
            ps.setInt(3, usuarioId);

            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            // Log error
            return false;
        }
    }

    @Override
    public boolean delete(Integer usuarioId) {
        String sql = "DELETE FROM usuarios WHERE id = ?";
        try (Connection conn = dbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);

            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            // Log error
            return false;
        }
    }

    @Override
    public boolean existsByUsername(String username) {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE username = ?";
        try (Connection conn = dbConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            // Log error
        }
        return false;
    }

    private Usuario mapResultSetToUsuario(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setId(rs.getInt("id"));
        usuario.setUsername(rs.getString("username"));
        usuario.setPasswordHash(rs.getString("password_hash"));
        usuario.setRol(Usuario.Role.valueOf(rs.getString("rol")));
        usuario.setActivo(rs.getBoolean("activo"));
        usuario.setMustChangePassword(rs.getBoolean("must_change_password"));

        java.sql.Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            usuario.setCreatedAt(createdAt.toLocalDateTime());
        }

        java.sql.Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            usuario.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        usuario.setCreatedBy(rs.getString("created_by"));
        usuario.setUpdatedBy(rs.getString("updated_by"));

        return usuario;
    }
}
