package com.example.roadmap.application.usecase;

import com.example.roadmap.domain.model.Roadmap;
import com.example.roadmap.domain.port.in.RoadmapCommandPort;
import com.example.roadmap.domain.port.in.RoadmapQueryPort;
import com.example.roadmap.domain.port.out.RoadmapRepositoryPort;

import java.util.List;
import java.util.Optional;

public class RoadmapUseCaseService implements RoadmapCommandPort, RoadmapQueryPort {
    private final RoadmapRepositoryPort repository;

    public RoadmapUseCaseService(RoadmapRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public Roadmap create(String title, String description) {
        Roadmap roadmap = new Roadmap(title, description);
        return repository.save(roadmap);
    }

    @Override
    public Optional<Roadmap> getById(String id) {
        return repository.findById(id);
    }

    @Override
    public List<Roadmap> list() {
        return repository.findAll();
    }
}
