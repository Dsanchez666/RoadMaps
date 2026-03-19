package com.example.roadmap.infrastructure.db;

import com.example.roadmap.domain.model.Roadmap;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RoadmapJdbcGateway {

    public List<Roadmap> findAll() {
        String sql = "SELECT id, titulo, descripcion, created_at FROM roadmaps";
        try (Connection conn = requireConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            List<Roadmap> out = new ArrayList<>();
            while (rs.next()) {
                out.add(mapRoadmap(rs));
            }
            return out;
        } catch (Exception e) {
            throw new IllegalStateException("Unable to query roadmaps", e);
        }
    }

    public Optional<Roadmap> findById(String id) {
        String sql = "SELECT id, titulo, descripcion, created_at FROM roadmaps WHERE id = ?";
        try (Connection conn = requireConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRoadmap(rs));
                }
                return Optional.empty();
            }
        } catch (Exception e) {
            throw new IllegalStateException("Unable to query roadmap by id", e);
        }
    }

    public Roadmap save(Roadmap roadmap) {
        String sql = "INSERT INTO roadmaps (id, titulo, descripcion, created_at) VALUES (?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE titulo = VALUES(titulo), descripcion = VALUES(descripcion)";
        try (Connection conn = requireConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roadmap.getId());
            ps.setString(2, roadmap.getTitle());
            ps.setString(3, roadmap.getDescription());
            ps.setTimestamp(4, java.sql.Timestamp.from(roadmap.getCreatedAt()));
            ps.executeUpdate();
            return roadmap;
        } catch (Exception e) {
            throw new IllegalStateException("Unable to save roadmap", e);
        }
    }

    private Connection requireConnection() throws Exception {
        Connection conn = DatabaseConnection.connect();
        if (conn == null || conn.isClosed()) {
            throw new IllegalStateException("Database connection is not available");
        }
        return conn;
    }

    private Roadmap mapRoadmap(ResultSet rs) throws Exception {
        String id = rs.getString("id");
        String titulo = rs.getString("titulo");
        String descripcion = rs.getString("descripcion");
        Instant createdAt = rs.getTimestamp("created_at").toInstant();
        return new Roadmap(id, titulo, descripcion, createdAt);
    }
}
