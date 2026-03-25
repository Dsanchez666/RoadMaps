package com.example.roadmap.adapters.out.persistence;

import com.example.roadmap.DatabaseConnection;
import com.example.roadmap.domain.Initiative;
import com.example.roadmap.domain.InitiativeDependency;
import com.example.roadmap.domain.RoadmapConfig;
import com.example.roadmap.domain.RoadmapConfigRepository;
import com.example.roadmap.domain.RoadmapHorizon;
import com.example.roadmap.domain.StrategicAxis;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * JDBC adapter persisting full roadmap configuration in MySQL tables.
 *
 * @since 1.0
 */
public class JdbcRoadmapConfigRepository implements RoadmapConfigRepository {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Logger LOG = LoggerFactory.getLogger(JdbcRoadmapConfigRepository.class);

    @Override
    public Optional<RoadmapConfig> findByRoadmapId(String roadmapId) {
        Connection connection = requireConnection();
        try {
            LOG.info("Cargando configuración de roadmap [{}]", roadmapId);
            RoadmapConfig config = readBaseConfig(connection, roadmapId);
            if (config == null) {
                LOG.warn("No existe roadmap base para id [{}]", roadmapId);
                return Optional.empty();
            }
            LOG.info("Cargando ejes estratégicos para roadmap [{}]", roadmapId);
            config.setEjes_estrategicos(readAxes(connection, roadmapId));
            LOG.info("Ejes cargados para roadmap [{}]: {}", roadmapId, config.getEjes_estrategicos().size());
            LOG.info("Cargando iniciativas para roadmap [{}]", roadmapId);
            config.setIniciativas(readInitiatives(connection, roadmapId));
            LOG.info("Iniciativas cargadas para roadmap [{}]: {}", roadmapId, config.getIniciativas().size());
            return Optional.of(config);
        } catch (Exception e) {
            LOG.error("Error cargando configuración para roadmap [{}]", roadmapId, e);
            throw new RuntimeException("No se pudo leer la configuración del roadmap", e);
        }
    }

    @Override
    public void saveForRoadmap(String roadmapId, RoadmapConfig config) {
        Connection connection = requireConnection();
        try {
            LOG.info("Guardando configuración de roadmap [{}]", roadmapId);
            connection.setAutoCommit(false);
            updateBaseConfig(connection, roadmapId, config);
            deleteInitiatives(connection, roadmapId);
            deleteAxes(connection, roadmapId);
            Map<String, String> axisIdMapping = insertAxes(connection, roadmapId, safeAxes(config));
            insertInitiatives(connection, roadmapId, safeInitiatives(config), axisIdMapping);
            connection.commit();
            LOG.info("Configuración guardada correctamente para roadmap [{}]. Ejes: {}, Iniciativas: {}",
                roadmapId, safeAxes(config).size(), safeInitiatives(config).size());
        } catch (Exception e) {
            rollbackQuietly(connection);
            LOG.error("Error guardando configuración para roadmap [{}]", roadmapId, e);
            throw new RuntimeException("No se pudo guardar la configuración del roadmap", e);
        } finally {
            resetAutocommitQuietly(connection);
        }
    }

    private RoadmapConfig readBaseConfig(Connection connection, String roadmapId) throws Exception {
        String sql = "SELECT producto, organizacion, horizonte_inicio, horizonte_fin FROM roadmaps WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, roadmapId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                RoadmapConfig config = new RoadmapConfig();
                config.setProducto(valueOrEmpty(rs.getString("producto")));
                config.setOrganizacion(valueOrEmpty(rs.getString("organizacion")));
                config.setHorizonte_base(new RoadmapHorizon(
                    valueOrEmpty(rs.getString("horizonte_inicio")),
                    valueOrEmpty(rs.getString("horizonte_fin"))
                ));
                return config;
            }
        }
    }

    private List<StrategicAxis> readAxes(Connection connection, String roadmapId) throws Exception {
        String sql = "SELECT id, nombre, descripcion, color FROM ejes_estrategicos WHERE roadmap_id = ? ORDER BY posicion ASC, created_at ASC";
        List<StrategicAxis> out = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, roadmapId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String storedId = valueOrEmpty(rs.getString("id"));
                    out.add(new StrategicAxis(
                        fromDbAxisId(roadmapId, storedId),
                        valueOrEmpty(rs.getString("nombre")),
                        valueOrEmpty(rs.getString("descripcion")),
                        valueOrEmpty(rs.getString("color"))
                    ));
                }
            }
        }
        return out;
    }

    private List<Initiative> readInitiatives(Connection connection, String roadmapId) throws Exception {
        String sql = "SELECT id, eje_id, nombre, inicio, fin, certeza, dependencias, informacion_adicional " +
            "FROM iniciativas WHERE roadmap_id = ? ORDER BY posicion ASC, created_at ASC";
        List<Initiative> out = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, roadmapId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Initiative initiative = new Initiative();
                    initiative.setId(valueOrEmpty(rs.getString("id")));
                    initiative.setEje(fromDbAxisId(roadmapId, valueOrEmpty(rs.getString("eje_id"))));
                    initiative.setNombre(valueOrEmpty(rs.getString("nombre")));
                    initiative.setInicio(valueOrEmpty(rs.getString("inicio")));
                    initiative.setFin(valueOrEmpty(rs.getString("fin")));
                    initiative.setCerteza(valueOrEmpty(rs.getString("certeza")));
                    initiative.setDependencias(readDependencies(rs.getString("dependencias")));
                    initiative.setInformacion_adicional(readAdditionalInfo(rs.getString("informacion_adicional")));
                    out.add(initiative);
                }
            }
        }
        return out;
    }

    private List<InitiativeDependency> readDependencies(String rawJson) {
        try {
            if (rawJson == null || rawJson.isBlank()) {
                return new ArrayList<>();
            }
            return OBJECT_MAPPER.readValue(rawJson, new TypeReference<List<InitiativeDependency>>() {
            });
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private Map<String, String> readAdditionalInfo(String rawJson) {
        try {
            if (rawJson == null || rawJson.isBlank()) {
                return new HashMap<>();
            }
            return OBJECT_MAPPER.readValue(rawJson, new TypeReference<Map<String, String>>() {
            });
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    private void updateBaseConfig(Connection connection, String roadmapId, RoadmapConfig config) throws Exception {
        String sql = "UPDATE roadmaps SET producto = ?, organizacion = ?, horizonte_inicio = ?, horizonte_fin = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, valueOrEmpty(config.getProducto()));
            ps.setString(2, valueOrEmpty(config.getOrganizacion()));
            ps.setString(3, valueOrEmpty(safeHorizon(config).getInicio()));
            ps.setString(4, valueOrEmpty(safeHorizon(config).getFin()));
            ps.setString(5, roadmapId);
            ps.executeUpdate();
        }
    }

    private void deleteInitiatives(Connection connection, String roadmapId) throws Exception {
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM iniciativas WHERE roadmap_id = ?")) {
            ps.setString(1, roadmapId);
            ps.executeUpdate();
        }
    }

    private void deleteAxes(Connection connection, String roadmapId) throws Exception {
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM ejes_estrategicos WHERE roadmap_id = ?")) {
            ps.setString(1, roadmapId);
            ps.executeUpdate();
        }
    }

    private Map<String, String> insertAxes(Connection connection, String roadmapId, List<StrategicAxis> axes) throws Exception {
        String sql = "INSERT INTO ejes_estrategicos (id, roadmap_id, nombre, descripcion, color, posicion) VALUES (?, ?, ?, ?, ?, ?)";
        Map<String, String> mapping = new HashMap<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (int i = 0; i < axes.size(); i++) {
                StrategicAxis axis = axes.get(i);
                String sourceId = valueOrEmpty(axis.getId());
                if (sourceId.isBlank()) {
                    sourceId = "AXIS-" + i;
                }
                if (mapping.containsKey(sourceId)) {
                    sourceId = sourceId + "-" + i;
                }
                String dbAxisId = toDbAxisId(roadmapId, sourceId);
                mapping.put(sourceId, dbAxisId);
                ps.setString(1, dbAxisId);
                ps.setString(2, roadmapId);
                ps.setString(3, valueOrEmpty(axis.getNombre()));
                ps.setString(4, valueOrEmpty(axis.getDescripcion()));
                ps.setString(5, valueOrEmpty(axis.getColor()));
                ps.setInt(6, i);
                ps.addBatch();
            }
            ps.executeBatch();
        }
        return mapping;
    }

    private void insertInitiatives(
        Connection connection,
        String roadmapId,
        List<Initiative> initiatives,
        Map<String, String> axisIdMapping
    ) throws Exception {
        String sql = "INSERT INTO iniciativas (id, roadmap_id, eje_id, nombre, inicio, fin, certeza, dependencias, informacion_adicional, posicion) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (int i = 0; i < initiatives.size(); i++) {
                Initiative initiative = initiatives.get(i);
                ps.setString(1, valueOrEmpty(initiative.getId()));
                ps.setString(2, roadmapId);
                String sourceAxisId = valueOrEmpty(initiative.getEje());
                String dbAxisId = axisIdMapping.get(sourceAxisId);
                if (dbAxisId == null || dbAxisId.isBlank()) {
                    ps.setNull(3, Types.VARCHAR);
                } else {
                    ps.setString(3, dbAxisId);
                }
                ps.setString(4, valueOrEmpty(initiative.getNombre()));
                ps.setString(5, valueOrEmpty(initiative.getInicio()));
                ps.setString(6, valueOrEmpty(initiative.getFin()));
                ps.setString(7, valueOrEmpty(initiative.getCerteza()));
                ps.setString(8, OBJECT_MAPPER.writeValueAsString(safeDependencies(initiative)));
                ps.setString(9, OBJECT_MAPPER.writeValueAsString(safeAdditionalInfo(initiative)));
                ps.setInt(10, i);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private List<StrategicAxis> safeAxes(RoadmapConfig config) {
        return (config.getEjes_estrategicos() != null) ? config.getEjes_estrategicos() : new ArrayList<>();
    }

    private List<Initiative> safeInitiatives(RoadmapConfig config) {
        return (config.getIniciativas() != null) ? config.getIniciativas() : new ArrayList<>();
    }

    private List<InitiativeDependency> safeDependencies(Initiative initiative) {
        return (initiative.getDependencias() != null) ? initiative.getDependencias() : new ArrayList<>();
    }

    private Map<String, String> safeAdditionalInfo(Initiative initiative) {
        if (initiative.getInformacion_adicional() == null) {
            return new HashMap<>();
        }
        return initiative.getInformacion_adicional();
    }

    private RoadmapHorizon safeHorizon(RoadmapConfig config) {
        return (config.getHorizonte_base() != null) ? config.getHorizonte_base() : new RoadmapHorizon("", "");
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    private String toDbAxisId(String roadmapId, String axisId) {
        return roadmapId + "::" + valueOrEmpty(axisId);
    }

    private String fromDbAxisId(String roadmapId, String storedId) {
        String prefix = roadmapId + "::";
        if (storedId != null && storedId.startsWith(prefix)) {
            return storedId.substring(prefix.length());
        }
        return valueOrEmpty(storedId);
    }

    private Connection requireConnection() {
        Connection connection = DatabaseConnection.getConnection();
        if (connection == null || !DatabaseConnection.isConnected()) {
            throw new IllegalStateException("No hay conexión activa con la base de datos.");
        }
        return connection;
    }

    private void rollbackQuietly(Connection connection) {
        try {
            connection.rollback();
        } catch (Exception ignored) {
            // no-op
        }
    }

    private void resetAutocommitQuietly(Connection connection) {
        try {
            connection.setAutoCommit(true);
        } catch (Exception ignored) {
            // no-op
        }
    }
}
