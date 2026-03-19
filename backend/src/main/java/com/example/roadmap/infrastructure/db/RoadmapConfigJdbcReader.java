package com.example.roadmap.infrastructure.db;

import com.example.roadmap.application.query.AxisView;
import com.example.roadmap.application.query.DependencyView;
import com.example.roadmap.application.query.HorizonView;
import com.example.roadmap.application.query.InitiativeView;
import com.example.roadmap.application.query.RoadmapConfigReader;
import com.example.roadmap.application.query.RoadmapConfigView;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RoadmapConfigJdbcReader implements RoadmapConfigReader {
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public RoadmapConfigView findById(String id) {
        try (Connection conn = DatabaseConnection.connect()) {
            if (conn == null || conn.isClosed()) {
                return null;
            }

            RoadmapConfigView base = loadRoadmap(conn, id);
            if (base == null) {
                return null;
            }

            List<AxisView> axes = loadAxes(conn, id);
            List<InitiativeView> inits = loadInitiatives(conn, id);

            return new RoadmapConfigView(
                    base.id(),
                    base.producto(),
                    base.organizacion(),
                    base.horizonte_base(),
                    axes,
                    inits
            );
        } catch (Exception e) {
            throw new IllegalStateException("Unable to load roadmap config", e);
        }
    }

    private RoadmapConfigView loadRoadmap(Connection conn, String id) throws Exception {
        String sql = "SELECT id, producto, organizacion, horizonte_inicio, horizonte_fin FROM roadmaps WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                HorizonView horizon = new HorizonView(rs.getString("horizonte_inicio"), rs.getString("horizonte_fin"));
                return new RoadmapConfigView(
                        rs.getString("id"),
                        rs.getString("producto"),
                        rs.getString("organizacion"),
                        horizon,
                        Collections.emptyList(),
                        Collections.emptyList()
                );
            }
        }
    }

    private List<AxisView> loadAxes(Connection conn, String roadmapId) throws Exception {
        String sql = "SELECT id, nombre, descripcion, color FROM ejes_estrategicos WHERE roadmap_id = ? ORDER BY posicion";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roadmapId);
            try (ResultSet rs = ps.executeQuery()) {
                List<AxisView> out = new ArrayList<>();
                while (rs.next()) {
                    out.add(new AxisView(
                            rs.getString("id"),
                            rs.getString("nombre"),
                            rs.getString("descripcion"),
                            rs.getString("color")
                    ));
                }
                return out;
            }
        }
    }

    private List<InitiativeView> loadInitiatives(Connection conn, String roadmapId) throws Exception {
        String sql = "SELECT id, eje_id, nombre, descripcion, inicio, fin, certeza, dependencias FROM iniciativas WHERE roadmap_id = ? ORDER BY posicion";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roadmapId);
            try (ResultSet rs = ps.executeQuery()) {
                List<InitiativeView> out = new ArrayList<>();
                while (rs.next()) {
                    List<DependencyView> deps = parseDependencies(rs.getString("dependencias"));
                    out.add(new InitiativeView(
                            rs.getString("id"),
                            rs.getString("nombre"),
                            rs.getString("eje_id"),
                            "",
                            "",
                            rs.getString("inicio"),
                            rs.getString("fin"),
                            rs.getString("descripcion"),
                            "",
                            "",
                            deps,
                            rs.getString("certeza")
                    ));
                }
                return out;
            }
        }
    }

    private List<DependencyView> parseDependencies(String raw) {
        if (raw == null || raw.isBlank()) {
            return Collections.emptyList();
        }
        try {
            List<DependencyView> deps = mapper.readValue(raw, new TypeReference<List<DependencyView>>() {});
            return deps == null ? Collections.emptyList() : deps;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
