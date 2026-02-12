package com.example.roadmap.domain;

import java.util.List;
import java.util.Optional;

public interface RoadmapRepository {
    Roadmap save(Roadmap roadmap);
    Optional<Roadmap> findById(String id);
    List<Roadmap> findAll();
}
