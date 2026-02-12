package com.example.roadmap.application;

import com.example.roadmap.domain.Roadmap;
import com.example.roadmap.domain.RoadmapRepository;

import java.util.List;
import java.util.Optional;

public class CreateRoadmapUseCase {
    private final RoadmapRepository repository;

    public CreateRoadmapUseCase(RoadmapRepository repository) {
        this.repository = repository;
    }

    public Roadmap create(String title, String description) {
        Roadmap roadmap = new Roadmap(title, description);
        return repository.save(roadmap);
    }

    public Optional<Roadmap> getById(String id) { return repository.findById(id); }
    public List<Roadmap> list() { return repository.findAll(); }
}
