package com.example.roadmap.application;

import com.example.roadmap.domain.Roadmap;
import com.example.roadmap.domain.RoadmapConfig;
import com.example.roadmap.domain.RoadmapConfigRepository;
import com.example.roadmap.domain.RoadmapHorizon;
import com.example.roadmap.domain.RoadmapRepository;

import java.util.Optional;

/**
 * Application service for roadmap configuration read/write operations.
 *
 * @since 1.0
 */
public class RoadmapConfigUseCase {
    private final RoadmapRepository roadmapRepository;
    private final RoadmapConfigRepository configRepository;

    public RoadmapConfigUseCase(RoadmapRepository roadmapRepository, RoadmapConfigRepository configRepository) {
        this.roadmapRepository = roadmapRepository;
        this.configRepository = configRepository;
    }

    /**
     * Returns roadmap configuration or builds a default one from roadmap data.
     *
     * @param roadmapId Roadmap identifier.
     * @return Optional<RoadmapConfig> existing or default config when roadmap exists.
     */
    public Optional<RoadmapConfig> getByRoadmapId(String roadmapId) {
        Optional<RoadmapConfig> existing = configRepository.findByRoadmapId(roadmapId);
        if (existing.isPresent()) {
            return existing;
        }
        return roadmapRepository.findById(roadmapId).map(this::buildDefaultConfig);
    }

    /**
     * Persists full roadmap configuration for one roadmap.
     *
     * @param roadmapId Roadmap identifier.
     * @param config Configuration payload.
     */
    public void saveForRoadmap(String roadmapId, RoadmapConfig config) {
        configRepository.saveForRoadmap(roadmapId, config);
    }

    private RoadmapConfig buildDefaultConfig(Roadmap roadmap) {
        RoadmapConfig config = new RoadmapConfig();
        config.setProducto(roadmap.getTitle());
        config.setOrganizacion("");
        config.setHorizonte_base(new RoadmapHorizon("", ""));
        return config;
    }
}
