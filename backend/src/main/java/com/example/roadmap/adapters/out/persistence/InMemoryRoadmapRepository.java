package com.example.roadmap.adapters.out.persistence;

import com.example.roadmap.domain.Roadmap;
import com.example.roadmap.domain.RoadmapRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryRoadmapRepository implements RoadmapRepository {
    private final Map<String, Roadmap> store = new ConcurrentHashMap<>();

    @Override
    public Roadmap save(Roadmap roadmap) {
        store.put(roadmap.getId(), roadmap);
        return roadmap;
    }

    @Override
    public Optional<Roadmap> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Roadmap> findAll() {
        return new ArrayList<>(store.values());
    }
}
