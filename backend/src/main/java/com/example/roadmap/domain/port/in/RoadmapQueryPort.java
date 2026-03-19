package com.example.roadmap.domain.port.in;

import com.example.roadmap.domain.model.Roadmap;

import java.util.List;
import java.util.Optional;

public interface RoadmapQueryPort {
    Optional<Roadmap> getById(String id);
    List<Roadmap> list();
}
