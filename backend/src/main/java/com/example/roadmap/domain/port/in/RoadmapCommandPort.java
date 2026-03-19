package com.example.roadmap.domain.port.in;

import com.example.roadmap.domain.model.Roadmap;

public interface RoadmapCommandPort {
    Roadmap create(String title, String description);
}
