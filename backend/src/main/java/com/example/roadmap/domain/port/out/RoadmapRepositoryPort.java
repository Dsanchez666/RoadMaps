package com.example.roadmap.domain.port.out;

import com.example.roadmap.domain.model.Roadmap;

import java.util.List;
import java.util.Optional;

public interface RoadmapRepositoryPort {
    Roadmap save(Roadmap roadmap);
    Optional<Roadmap> findById(String id);
    List<Roadmap> findAll();
}
