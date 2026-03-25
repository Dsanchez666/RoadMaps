package com.example.roadmap.adapters.in.web;

import com.example.roadmap.application.CreateRoadmapUseCase;
import com.example.roadmap.application.RoadmapConfigUseCase;
import com.example.roadmap.adapters.out.persistence.JdbcRoadmapConfigRepository;
import com.example.roadmap.adapters.out.persistence.JdbcRoadmapRepository;
import com.example.roadmap.domain.Roadmap;
import com.example.roadmap.domain.RoadmapConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST controller exposing roadmap and roadmap-config operations.
 *
 * @since 1.0
 */
@RestController
@RequestMapping("/api/roadmaps")
public class RoadmapController {
    private static final Logger LOG = LoggerFactory.getLogger(RoadmapController.class);
    private final JdbcRoadmapRepository roadmapRepository;
    private final JdbcRoadmapConfigRepository configRepository;
    private final CreateRoadmapUseCase roadmapUseCase;
    private final RoadmapConfigUseCase roadmapConfigUseCase;

    public RoadmapController() {
        this.roadmapRepository = new JdbcRoadmapRepository();
        this.configRepository = new JdbcRoadmapConfigRepository();
        this.roadmapUseCase = new CreateRoadmapUseCase(roadmapRepository);
        this.roadmapConfigUseCase = new RoadmapConfigUseCase(roadmapRepository, configRepository);
    }

    /**
     * Creates a roadmap from a basic payload.
     */
    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateDto dto) {
        try {
            LOG.info("Solicitud crear roadmap recibida");
            if (dto == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Payload vacío."));
            }
            String title = safe(dto.title);
            if (title.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("message", "El título es obligatorio."));
            }
            Roadmap created = roadmapUseCase.create(title, safe(dto.description));
            LOG.info("Roadmap creado [{}] título [{}]", created.getId(), created.getTitle());
            return ResponseEntity.created(URI.create("/api/roadmaps/" + created.getId())).body(created);
        } catch (IllegalStateException e) {
            LOG.warn("Crear roadmap falló por estado de conexión: {}", e.getMessage());
            return ResponseEntity.status(503).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            LOG.error("Error creando roadmap", e);
            return ResponseEntity.status(500).body(Map.of("message", "No se pudo crear roadmap."));
        }
    }

    /**
     * Imports one full roadmap payload including editable config.
     */
    @PostMapping("/import")
    public ResponseEntity<?> importRoadmap(@RequestBody ImportRoadmapDto dto) {
        try {
            LOG.info("Solicitud importación roadmap recibida");
            if (dto == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Payload vacío."));
            }
            String title = safe(dto.title);
            if (title.isBlank()) {
                title = safe(dto.producto);
            }
            if (title.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("message", "El título o producto es obligatorio."));
            }

            Roadmap created = roadmapUseCase.create(title, safe(dto.description));
            RoadmapConfig config = dto.toRoadmapConfig();
            roadmapConfigUseCase.saveForRoadmap(created.getId(), config);
            LOG.info("Roadmap importado [{}], ejes: {}, iniciativas: {}",
                created.getId(), config.getEjes_estrategicos().size(), config.getIniciativas().size());
            return ResponseEntity.created(URI.create("/api/roadmaps/" + created.getId())).body(created);
        } catch (IllegalStateException e) {
            LOG.warn("Importación roadmap falló por estado de conexión: {}", e.getMessage());
            return ResponseEntity.status(503).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            LOG.error("Error importando roadmap", e);
            return ResponseEntity.status(500).body(Map.of("message", "No se pudo importar roadmap."));
        }
    }

    /**
     * Lists all roadmaps.
     */
    @GetMapping
    public ResponseEntity<?> list() {
        try {
            LOG.info("Solicitud listar roadmaps");
            List<Roadmap> roadmaps = roadmapUseCase.list();
            LOG.info("Roadmaps listados: {}", roadmaps.size());
            return ResponseEntity.ok(roadmaps);
        } catch (IllegalStateException e) {
            LOG.warn("Listado roadmaps falló por estado de conexión: {}", e.getMessage());
            return ResponseEntity.status(503).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            LOG.error("Error listando roadmaps", e);
            return ResponseEntity.status(500).body(Map.of("message", "No se pudo listar roadmaps."));
        }
    }

    /**
     * Returns one roadmap by id.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable String id) {
        try {
            LOG.info("Solicitud get roadmap [{}]", id);
            Optional<Roadmap> roadmap = roadmapUseCase.getById(id);
            return roadmap.<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (IllegalStateException e) {
            LOG.warn("Get roadmap [{}] falló por estado de conexión: {}", id, e.getMessage());
            return ResponseEntity.status(503).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            LOG.error("Error obteniendo roadmap [{}]", id, e);
            return ResponseEntity.status(500).body(Map.of("message", "No se pudo recuperar roadmap."));
        }
    }

    /**
     * Returns full editable configuration for one roadmap.
     */
    @GetMapping("/{id}/config")
    public ResponseEntity<?> getConfig(@PathVariable String id) {
        try {
            LOG.info("Solicitud get configuración roadmap [{}]", id);
            Optional<RoadmapConfig> config = roadmapConfigUseCase.getByRoadmapId(id);
            return config.<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (IllegalStateException e) {
            LOG.warn("Get config roadmap [{}] falló por estado de conexión: {}", id, e.getMessage());
            return ResponseEntity.status(503).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            LOG.error("Error obteniendo configuración de roadmap [{}]", id, e);
            return ResponseEntity.status(500).body(Map.of("message", "No se pudo recuperar la configuración."));
        }
    }

    /**
     * Persists editable configuration for one roadmap.
     */
    @PutMapping("/{id}/config")
    public ResponseEntity<?> saveConfig(@PathVariable String id, @RequestBody RoadmapConfig config) {
        try {
            LOG.info("Solicitud guardar configuración roadmap [{}]", id);
            Optional<Roadmap> roadmap = roadmapUseCase.getById(id);
            if (roadmap.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            roadmapConfigUseCase.saveForRoadmap(id, config);
            LOG.info("Configuración guardada para roadmap [{}]", id);
            return ResponseEntity.ok(Map.of("status", "SUCCESS"));
        } catch (IllegalStateException e) {
            LOG.warn("Guardar config roadmap [{}] falló por estado de conexión: {}", id, e.getMessage());
            return ResponseEntity.status(503).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            LOG.error("Error guardando configuración de roadmap [{}]", id, e);
            return ResponseEntity.status(500).body(Map.of("message", "No se pudo guardar la configuración."));
        }
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * DTO for roadmap creation requests.
     */
    public static class CreateDto {
        public String title;
        public String description;
    }

    /**
     * DTO for full JSON roadmap import requests.
     */
    public static class ImportRoadmapDto {
        public String title;
        public String description;
        public String producto;
        public String organizacion;
        public com.example.roadmap.domain.RoadmapHorizon horizonte_base;
        public List<com.example.roadmap.domain.StrategicAxis> ejes_estrategicos;
        public List<com.example.roadmap.domain.Initiative> iniciativas;

        public RoadmapConfig toRoadmapConfig() {
            RoadmapConfig config = new RoadmapConfig();
            config.setProducto(producto == null || producto.isBlank() ? title : producto);
            config.setOrganizacion(organizacion == null ? "" : organizacion);
            config.setHorizonte_base(horizonte_base == null
                ? new com.example.roadmap.domain.RoadmapHorizon("", "")
                : horizonte_base);
            config.setEjes_estrategicos(ejes_estrategicos == null ? List.of() : ejes_estrategicos);
            config.setIniciativas(iniciativas == null ? List.of() : iniciativas);
            return config;
        }
    }
}
