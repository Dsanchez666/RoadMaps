package com.example.roadmap.domain;

import java.util.Optional;

/**
 * Port interface for roadmap configuration persistence.
 *
 * @since 1.0
 */
public interface RoadmapConfigRepository {

    /**
     * Returns saved roadmap configuration for the provided roadmap id.
     *
     * @param roadmapId Roadmap identifier.
     * @return Optional<RoadmapConfig> persisted configuration when available.
     */
    Optional<RoadmapConfig> findByRoadmapId(String roadmapId);

    /**
     * Persists full roadmap configuration.
     *
     * @param roadmapId Roadmap identifier owning the config.
     * @param config Configuration payload to persist.
     */
    void saveForRoadmap(String roadmapId, RoadmapConfig config);
}
