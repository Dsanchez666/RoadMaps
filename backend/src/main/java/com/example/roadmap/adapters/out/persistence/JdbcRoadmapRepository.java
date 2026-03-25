package com.example.roadmap.adapters.out.persistence;

import com.example.roadmap.DatabaseConnection;
import com.example.roadmap.domain.Roadmap;
import com.example.roadmap.domain.RoadmapRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of roadmap persistence backed by relational tables.
 *
 * @since 1.0
 */
public class JdbcRoadmapRepository implements RoadmapRepository {

    @Override
    public Roadmap save(Roadmap roadmap) {
        String sql = "INSERT INTO roadmaps (id, titulo, descripcion, created_at, updated_at) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";
        try (PreparedStatement ps = requireConnection().prepareStatement(sql)) {
            ps.setString(1, roadmap.getId());
            ps.setString(2, roadmap.getTitle());
            ps.setString(3, roadmap.getDescription());
            ps.setTimestamp(4, Timestamp.from(roadmap.getCreatedAt()));
            ps.executeUpdate();
            return roadmap;
        } catch (Exception e) {
            throw new RuntimeException("No se pudo guardar el roadmap", e);
        }
    }

    @Override
    public Optional<Roadmap> findById(String id) {
        String sql = "SELECT id, titulo, descripcion, created_at FROM roadmaps WHERE id = ?";
        try (PreparedStatement ps = requireConnection().prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapRoadmap(rs));
            }
        } catch (Exception e) {
            throw new RuntimeException("No se pudo leer el roadmap", e);
        }
    }

    @Override
    public List<Roadmap> findAll() {
        String sql = "SELECT id, titulo, descripcion, created_at FROM roadmaps ORDER BY created_at DESC";
        List<Roadmap> out = new ArrayList<>();
        try (PreparedStatement ps = requireConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(mapRoadmap(rs));
            }
            return out;
        } catch (Exception e) {
            throw new RuntimeException("No se pudo listar roadmaps", e);
        }
    }

    private Roadmap mapRoadmap(ResultSet rs) throws Exception {
        String id = rs.getString("id");
        String title = rs.getString("titulo");
        String description = rs.getString("descripcion");
        Timestamp createdAtTs = rs.getTimestamp("created_at");
        Instant createdAt = (createdAtTs != null) ? createdAtTs.toInstant() : Instant.now();
        return new Roadmap(id, title, description, createdAt);
    }

    private Connection requireConnection() {
        Connection connection = DatabaseConnection.getConnection();
        if (connection == null || !DatabaseConnection.isConnected()) {
            throw new IllegalStateException("No hay conexión activa con la base de datos.");
        }
        return connection;
    }
}
