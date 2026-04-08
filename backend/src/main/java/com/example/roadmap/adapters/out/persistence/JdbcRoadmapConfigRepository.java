package com.example.roadmap.adapters.out.persistence;

import com.example.roadmap.DatabaseConnection;
import com.example.roadmap.domain.Initiative;
import com.example.roadmap.domain.InitiativeDependency;
import com.example.roadmap.domain.InitiativeExpediente;
import com.example.roadmap.domain.RoadmapConfig;
import com.example.roadmap.domain.RoadmapConfigRepository;
import com.example.roadmap.domain.RoadmapCommitment;
import com.example.roadmap.domain.RoadmapHorizon;
import com.example.roadmap.domain.StrategicAxis;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * JDBC adapter persisting full roadmap configuration in MySQL tables.
 *
 * <p>
 * Expedientes are stored in normalized tables and legacy embedded JSON data is
 * migrated automatically on read when needed.
 * </p>
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
            RoadmapConfig config = readBaseConfig(connection, roadmapId);
            if (config == null) {
                return Optional.empty();
            }

            config.setEjes_estrategicos(readAxes(connection, roadmapId));
            List<Initiative> initiatives = readInitiatives(connection, roadmapId);

            if (supportsNormalizedExpedientes(connection)) {
                migrateLegacyExpedientesIfNeeded(connection, roadmapId, initiatives);
                Map<String, List<InitiativeExpediente>> linkedExpedientes = readInitiativeExpedientes(connection, roadmapId);
                for (Initiative initiative : initiatives) {
                    initiative.setExpedientes(linkedExpedientes.getOrDefault(initiative.getId(), new ArrayList<>()));
                }
                config.setExpedientes_catalogo(readExpedientesCatalog(connection));
            } else {
                config.setExpedientes_catalogo(buildCatalogFromInitiatives(initiatives));
            }

            config.setIniciativas(initiatives);
            config.setCompromisos(readCommitments(connection, roadmapId));
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
            connection.setAutoCommit(false);
            updateBaseConfig(connection, roadmapId, config);
            deleteCommitments(connection, roadmapId);
            deleteInitiatives(connection, roadmapId);
            deleteAxes(connection, roadmapId);

            Map<String, String> axisIdMapping = insertAxes(connection, roadmapId, safeAxes(config));
            insertInitiatives(connection, roadmapId, safeInitiatives(config), axisIdMapping);

            if (supportsNormalizedExpedientes(connection)) {
                upsertExpedientes(connection, safeInitiatives(config));
                insertInitiativeExpedienteLinks(connection, safeInitiatives(config));
            }

            insertCommitments(connection, roadmapId, safeCommitments(config));
            connection.commit();
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
        String sql = "SELECT id, eje_id, nombre, inicio, fin, certeza, dependencias, informacion_adicional, expedientes " +
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
                    initiative.setExpedientes(readLegacyExpedientes(rs.getString("expedientes"), initiative.getInformacion_adicional()));
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
                return new LinkedHashMap<>();
            }
            return OBJECT_MAPPER.readValue(rawJson, new TypeReference<Map<String, String>>() {
            });
        } catch (Exception e) {
            return new LinkedHashMap<>();
        }
    }

    private List<InitiativeExpediente> readLegacyExpedientes(String rawJson, Map<String, String> additionalInfo) {
        try {
            if (rawJson != null && !rawJson.isBlank()) {
                return normalizeExpedienteIds(OBJECT_MAPPER.readValue(rawJson, new TypeReference<List<InitiativeExpediente>>() {
                }));
            }
        } catch (Exception ignored) {
            // legacy fallback below
        }

        InitiativeExpediente fallback = buildFallbackExpediente(additionalInfo);
        if (fallback == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(List.of(fallback));
    }

    private InitiativeExpediente buildFallbackExpediente(Map<String, String> additionalInfo) {
        if (additionalInfo == null || additionalInfo.isEmpty()) {
            return null;
        }
        InitiativeExpediente out = new InitiativeExpediente();
        out.setTipo(valueOrEmpty(additionalInfo.get("tipo")));
        out.setEmpresa(valueOrEmpty(additionalInfo.get("empresa")));
        out.setExpediente(valueOrEmpty(additionalInfo.get("expediente")));
        out.setImpacto(valueOrEmpty(additionalInfo.get("impacto_principal")));
        out.setPrecio_licitacion(valueOrEmpty(additionalInfo.get("precio_licitacion")));
        out.setPrecio_adjudicacion(valueOrEmpty(additionalInfo.get("precio_adjudicacion")));
        out.setFecha_fin_expediente(valueOrEmpty(additionalInfo.get("fecha_fin_expediente")));
        out.setInformacion_adicional(new LinkedHashMap<>());
        if (isBlank(out.getExpediente()) && isBlank(out.getEmpresa()) && isBlank(out.getPrecio_licitacion())
            && isBlank(out.getPrecio_adjudicacion()) && isBlank(out.getFecha_fin_expediente())) {
            return null;
        }
        out.setId(generateExpedienteId(out));
        return out;
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

    private void deleteCommitments(Connection connection, String roadmapId) throws Exception {
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM compromisos WHERE roadmap_id = ?")) {
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

    private void insertInitiatives(Connection connection, String roadmapId, List<Initiative> initiatives, Map<String, String> axisIdMapping) throws Exception {
        String sql = "INSERT INTO iniciativas (id, roadmap_id, eje_id, nombre, inicio, fin, certeza, dependencias, informacion_adicional, expedientes, posicion) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
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
                ps.setNull(10, Types.VARCHAR);
                ps.setInt(11, i);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private List<RoadmapCommitment> readCommitments(Connection connection, String roadmapId) throws Exception {
        String sql = "SELECT id, descripcion, fecha_comprometido, actor, quien_compromete, informacion_adicional " +
            "FROM compromisos WHERE roadmap_id = ? ORDER BY posicion ASC, created_at ASC";
        List<RoadmapCommitment> out = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, roadmapId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    RoadmapCommitment commitment = new RoadmapCommitment();
                    commitment.setId(valueOrEmpty(rs.getString("id")));
                    commitment.setDescripcion(valueOrEmpty(rs.getString("descripcion")));
                    commitment.setFecha_comprometido(valueOrEmpty(rs.getString("fecha_comprometido")));
                    commitment.setActor(valueOrEmpty(rs.getString("actor")));
                    commitment.setQuien_compromete(valueOrEmpty(rs.getString("quien_compromete")));
                    commitment.setInformacion_adicional(readAdditionalInfo(rs.getString("informacion_adicional")));
                    out.add(commitment);
                }
            }
        }
        return out;
    }

    private void insertCommitments(Connection connection, String roadmapId, List<RoadmapCommitment> commitments) throws Exception {
        String sql = "INSERT INTO compromisos (id, roadmap_id, descripcion, fecha_comprometido, actor, quien_compromete, informacion_adicional, posicion) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (int i = 0; i < commitments.size(); i++) {
                RoadmapCommitment commitment = commitments.get(i);
                ps.setString(1, valueOrEmpty(commitment.getId()));
                ps.setString(2, roadmapId);
                ps.setString(3, valueOrEmpty(commitment.getDescripcion()));
                ps.setString(4, valueOrEmpty(commitment.getFecha_comprometido()));
                ps.setString(5, valueOrEmpty(commitment.getActor()));
                ps.setString(6, valueOrEmpty(commitment.getQuien_compromete()));
                ps.setString(7, OBJECT_MAPPER.writeValueAsString(safeAdditionalInfo(commitment)));
                ps.setInt(8, i);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private boolean supportsNormalizedExpedientes(Connection connection) throws Exception {
        return tableExists(connection, "expedientes") && tableExists(connection, "iniciativa_expediente");
    }

    private boolean tableExists(Connection connection, String tableName) throws Exception {
        String sql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, tableName);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    private void migrateLegacyExpedientesIfNeeded(Connection connection, String roadmapId, List<Initiative> initiatives) throws Exception {
        if (initiatives.isEmpty()) {
            return;
        }
        boolean hasLegacy = initiatives.stream().anyMatch(initiative -> !initiative.getExpedientes().isEmpty());
        if (!hasLegacy) {
            return;
        }

        String sql = """
            SELECT COUNT(*)
            FROM iniciativa_expediente ie
            INNER JOIN iniciativas i ON i.id = ie.iniciativa_id
            WHERE i.roadmap_id = ?
            """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, roadmapId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                if (rs.getInt(1) > 0) {
                    return;
                }
            }
        }

        LOG.info("Migrando expedientes legacy de roadmap [{}] al modelo normalizado", roadmapId);
        upsertExpedientes(connection, initiatives);
        insertInitiativeExpedienteLinks(connection, initiatives);
    }

    private void upsertExpedientes(Connection connection, List<Initiative> initiatives) throws Exception {
        String sql = "INSERT INTO expedientes (id, tipo, empresa, expediente, impacto, precio_licitacion, precio_adjudicacion, fecha_fin_expediente, informacion_adicional, huella_negocio) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
            "ON DUPLICATE KEY UPDATE tipo = VALUES(tipo), empresa = VALUES(empresa), expediente = VALUES(expediente), impacto = VALUES(impacto), " +
            "precio_licitacion = VALUES(precio_licitacion), precio_adjudicacion = VALUES(precio_adjudicacion), fecha_fin_expediente = VALUES(fecha_fin_expediente), " +
            "informacion_adicional = VALUES(informacion_adicional), huella_negocio = VALUES(huella_negocio)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            Map<String, InitiativeExpediente> uniques = collectUniqueExpedientes(initiatives);
            for (InitiativeExpediente expediente : uniques.values()) {
                ensureExpedienteIdentity(expediente);
                ps.setString(1, expediente.getId());
                ps.setString(2, valueOrEmpty(expediente.getTipo()));
                ps.setString(3, valueOrEmpty(expediente.getEmpresa()));
                ps.setString(4, valueOrEmpty(expediente.getExpediente()));
                ps.setString(5, valueOrEmpty(expediente.getImpacto()));
                ps.setString(6, valueOrEmpty(expediente.getPrecio_licitacion()));
                ps.setString(7, valueOrEmpty(expediente.getPrecio_adjudicacion()));
                ps.setString(8, valueOrEmpty(expediente.getFecha_fin_expediente()));
                ps.setString(9, OBJECT_MAPPER.writeValueAsString(safeAdditionalInfo(expediente)));
                ps.setString(10, buildBusinessFingerprint(expediente));
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void insertInitiativeExpedienteLinks(Connection connection, List<Initiative> initiatives) throws Exception {
        String sql = "INSERT INTO iniciativa_expediente (iniciativa_id, expediente_id, posicion) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (Initiative initiative : initiatives) {
                List<InitiativeExpediente> normalized = normalizeExpedienteIds(initiative.getExpedientes());
                Set<String> seen = new LinkedHashSet<>();
                for (int i = 0; i < normalized.size(); i++) {
                    InitiativeExpediente expediente = normalized.get(i);
                    ensureExpedienteIdentity(expediente);
                    if (!seen.add(expediente.getId())) {
                        continue;
                    }
                    ps.setString(1, valueOrEmpty(initiative.getId()));
                    ps.setString(2, expediente.getId());
                    ps.setInt(3, i);
                    ps.addBatch();
                }
                initiative.setExpedientes(normalized);
            }
            ps.executeBatch();
        }
    }

    private Map<String, List<InitiativeExpediente>> readInitiativeExpedientes(Connection connection, String roadmapId) throws Exception {
        String sql = """
            SELECT ie.iniciativa_id, e.id, e.tipo, e.empresa, e.expediente, e.impacto, e.precio_licitacion,
                   e.precio_adjudicacion, e.fecha_fin_expediente, e.informacion_adicional
            FROM iniciativa_expediente ie
            INNER JOIN iniciativas i ON i.id = ie.iniciativa_id
            INNER JOIN expedientes e ON e.id = ie.expediente_id
            WHERE i.roadmap_id = ?
            ORDER BY i.posicion ASC, ie.posicion ASC, ie.created_at ASC
            """;
        Map<String, List<InitiativeExpediente>> out = new HashMap<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, roadmapId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    InitiativeExpediente expediente = new InitiativeExpediente();
                    expediente.setId(valueOrEmpty(rs.getString("id")));
                    expediente.setTipo(valueOrEmpty(rs.getString("tipo")));
                    expediente.setEmpresa(valueOrEmpty(rs.getString("empresa")));
                    expediente.setExpediente(valueOrEmpty(rs.getString("expediente")));
                    expediente.setImpacto(valueOrEmpty(rs.getString("impacto")));
                    expediente.setPrecio_licitacion(valueOrEmpty(rs.getString("precio_licitacion")));
                    expediente.setPrecio_adjudicacion(valueOrEmpty(rs.getString("precio_adjudicacion")));
                    expediente.setFecha_fin_expediente(valueOrEmpty(rs.getString("fecha_fin_expediente")));
                    expediente.setInformacion_adicional(readAdditionalInfo(rs.getString("informacion_adicional")));
                    out.computeIfAbsent(valueOrEmpty(rs.getString("iniciativa_id")), key -> new ArrayList<>()).add(expediente);
                }
            }
        }
        return out;
    }

    private List<InitiativeExpediente> readExpedientesCatalog(Connection connection) throws Exception {
        String sql = "SELECT id, tipo, empresa, expediente, impacto, precio_licitacion, precio_adjudicacion, fecha_fin_expediente, informacion_adicional " +
            "FROM expedientes ORDER BY expediente ASC, empresa ASC, created_at ASC";
        List<InitiativeExpediente> out = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                InitiativeExpediente expediente = new InitiativeExpediente();
                expediente.setId(valueOrEmpty(rs.getString("id")));
                expediente.setTipo(valueOrEmpty(rs.getString("tipo")));
                expediente.setEmpresa(valueOrEmpty(rs.getString("empresa")));
                expediente.setExpediente(valueOrEmpty(rs.getString("expediente")));
                expediente.setImpacto(valueOrEmpty(rs.getString("impacto")));
                expediente.setPrecio_licitacion(valueOrEmpty(rs.getString("precio_licitacion")));
                expediente.setPrecio_adjudicacion(valueOrEmpty(rs.getString("precio_adjudicacion")));
                expediente.setFecha_fin_expediente(valueOrEmpty(rs.getString("fecha_fin_expediente")));
                expediente.setInformacion_adicional(readAdditionalInfo(rs.getString("informacion_adicional")));
                out.add(expediente);
            }
        }
        return out;
    }

    private List<InitiativeExpediente> buildCatalogFromInitiatives(List<Initiative> initiatives) {
        return new ArrayList<>(collectUniqueExpedientes(initiatives).values());
    }

    private Map<String, InitiativeExpediente> collectUniqueExpedientes(List<Initiative> initiatives) {
        Map<String, InitiativeExpediente> uniques = new LinkedHashMap<>();
        for (Initiative initiative : initiatives) {
            for (InitiativeExpediente expediente : normalizeExpedienteIds(initiative.getExpedientes())) {
                InitiativeExpediente candidate = cloneExpediente(expediente);
                String key = isBlank(candidate.getId()) ? "fp:" + buildBusinessFingerprint(candidate) : "id:" + candidate.getId();
                uniques.putIfAbsent(key, candidate);
            }
        }
        return uniques;
    }

    private List<InitiativeExpediente> normalizeExpedienteIds(List<InitiativeExpediente> expedientes) {
        List<InitiativeExpediente> out = new ArrayList<>();
        if (expedientes == null) {
            return out;
        }
        for (InitiativeExpediente expediente : expedientes) {
            InitiativeExpediente copy = cloneExpediente(expediente);
            ensureExpedienteIdentity(copy);
            out.add(copy);
        }
        return out;
    }

    private InitiativeExpediente cloneExpediente(InitiativeExpediente source) {
        InitiativeExpediente copy = new InitiativeExpediente();
        if (source == null) {
            return copy;
        }
        copy.setId(valueOrEmpty(source.getId()));
        copy.setTipo(valueOrEmpty(source.getTipo()));
        copy.setEmpresa(valueOrEmpty(source.getEmpresa()));
        copy.setExpediente(valueOrEmpty(source.getExpediente()));
        copy.setImpacto(valueOrEmpty(source.getImpacto()));
        copy.setPrecio_licitacion(valueOrEmpty(source.getPrecio_licitacion()));
        copy.setPrecio_adjudicacion(valueOrEmpty(source.getPrecio_adjudicacion()));
        copy.setFecha_fin_expediente(valueOrEmpty(source.getFecha_fin_expediente()));
        copy.setInformacion_adicional(safeAdditionalInfo(source));
        return copy;
    }

    private void ensureExpedienteIdentity(InitiativeExpediente expediente) {
        if (!isBlank(expediente.getId())) {
            return;
        }
        expediente.setId(generateExpedienteId(expediente));
    }

    private String generateExpedienteId(InitiativeExpediente expediente) {
        String fingerprint = buildBusinessFingerprint(expediente);
        return "EXP-" + UUID.nameUUIDFromBytes(fingerprint.getBytes(StandardCharsets.UTF_8)).toString();
    }

    private String buildBusinessFingerprint(InitiativeExpediente expediente) {
        return String.join("|",
            valueOrEmpty(expediente.getTipo()).trim().toLowerCase(),
            valueOrEmpty(expediente.getEmpresa()).trim().toLowerCase(),
            valueOrEmpty(expediente.getExpediente()).trim().toLowerCase(),
            valueOrEmpty(expediente.getImpacto()).trim().toLowerCase(),
            valueOrEmpty(expediente.getPrecio_licitacion()).trim().toLowerCase(),
            valueOrEmpty(expediente.getPrecio_adjudicacion()).trim().toLowerCase(),
            valueOrEmpty(expediente.getFecha_fin_expediente()).trim().toLowerCase(),
            normalizeAdditionalInfo(expediente.getInformacion_adicional())
        );
    }

    private String normalizeAdditionalInfo(Map<String, String> info) {
        if (info == null || info.isEmpty()) {
            return "";
        }
        return info.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(entry -> entry.getKey() + "=" + valueOrEmpty(entry.getValue()))
            .reduce((left, right) -> left + ";" + right)
            .orElse("");
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private List<StrategicAxis> safeAxes(RoadmapConfig config) {
        return (config.getEjes_estrategicos() != null) ? config.getEjes_estrategicos() : new ArrayList<>();
    }

    private List<Initiative> safeInitiatives(RoadmapConfig config) {
        return (config.getIniciativas() != null) ? config.getIniciativas() : new ArrayList<>();
    }

    private List<RoadmapCommitment> safeCommitments(RoadmapConfig config) {
        return (config.getCompromisos() != null) ? config.getCompromisos() : new ArrayList<>();
    }

    private List<InitiativeDependency> safeDependencies(Initiative initiative) {
        return (initiative.getDependencias() != null) ? initiative.getDependencias() : new ArrayList<>();
    }

    private Map<String, String> safeAdditionalInfo(Initiative initiative) {
        return initiative.getInformacion_adicional() == null ? new LinkedHashMap<>() : new LinkedHashMap<>(initiative.getInformacion_adicional());
    }

    private Map<String, String> safeAdditionalInfo(InitiativeExpediente expediente) {
        return expediente.getInformacion_adicional() == null ? new LinkedHashMap<>() : new LinkedHashMap<>(expediente.getInformacion_adicional());
    }

    private Map<String, String> safeAdditionalInfo(RoadmapCommitment commitment) {
        return commitment.getInformacion_adicional() == null ? new LinkedHashMap<>() : new LinkedHashMap<>(commitment.getInformacion_adicional());
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
