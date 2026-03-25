package com.example.roadmap.application;

import com.example.roadmap.domain.Roadmap;
import com.example.roadmap.domain.RoadmapRepository;

import java.util.List;
import java.util.Optional;

/**
 * Application use case for creating and querying roadmaps.
 *
 * This class contains orchestration logic and delegates persistence to the
 * repository port.
 *
 * @since 1.0
 */
public class CreateRoadmapUseCase {
    private final RoadmapRepository repository;

    /**
     * Creates a use case instance with a repository implementation.
     *
     * @param repository Port used to store and read roadmaps.
     */
    public CreateRoadmapUseCase(RoadmapRepository repository) {
        this.repository = repository;
    }

    /**
     * Creates and persists a new roadmap.
     *
     * @param title Roadmap title.
     * @param description Roadmap description.
     * @return Created roadmap.
     */
    public Roadmap create(String title, String description) {
        Roadmap roadmap = new Roadmap(title, description);
        return repository.save(roadmap);
    }

    /**
     * Returns a roadmap by id.
     *
     * @param id Roadmap identifier.
     * @return Optional roadmap.
     */
    public Optional<Roadmap> getById(String id) { return repository.findById(id); }

    /**
     * Returns all stored roadmaps.
     *
     * @return List of roadmaps.
     */
    public List<Roadmap> list() { return repository.findAll(); }
}