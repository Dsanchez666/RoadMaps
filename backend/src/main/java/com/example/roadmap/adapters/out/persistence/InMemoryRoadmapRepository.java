package com.example.roadmap.adapters.out.persistence;

import com.example.roadmap.domain.Roadmap;
import com.example.roadmap.domain.RoadmapRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory repository implementation for development/testing scenarios.
 *
 * This adapter stores data in a thread-safe map and does not persist data
 * between application restarts.
 *
 * @since 1.0
 */
public class InMemoryRoadmapRepository implements RoadmapRepository {
    private final Map<String, Roadmap> store = new ConcurrentHashMap<>();

    /**
     * Stores or replaces a roadmap by id.
     *
     * @param roadmap Roadmap to save.
     * @return Saved roadmap.
     */
    @Override
    public Roadmap save(Roadmap roadmap) {
        store.put(roadmap.getId(), roadmap);
        return roadmap;
    }

    /**
     * Finds a roadmap in memory by id.
     *
     * @param id Roadmap identifier.
     * @return Optional roadmap.
     */
    @Override
    public Optional<Roadmap> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    /**
     * Returns all currently stored roadmaps.
     *
     * @return Snapshot list of roadmaps.
     */
    @Override
    public List<Roadmap> findAll() {
        return new ArrayList<>(store.values());
    }
}