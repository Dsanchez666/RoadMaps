package com.example.roadmap.domain;

import java.util.List;
import java.util.Optional;

/**
 * Port interface for roadmap persistence operations.
 *
 * Use cases depend on this interface and remain agnostic of storage technology.
 *
 * @since 1.0
 */
public interface RoadmapRepository {

    /**
     * Persists a roadmap instance.
     *
     * @param roadmap Domain object to save.
     * @return Saved roadmap.
     */
    Roadmap save(Roadmap roadmap);

    /**
     * Finds a roadmap by id.
     *
     * @param id Roadmap identifier.
     * @return Optional containing the roadmap when found.
     */
    Optional<Roadmap> findById(String id);

    /**
     * Returns all stored roadmaps.
     *
     * @return List of roadmaps.
     */
    List<Roadmap> findAll();
}