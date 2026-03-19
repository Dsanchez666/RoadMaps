package com.example.roadmap.infrastructure.adapter.in.web.mapper;

import com.example.roadmap.domain.model.Roadmap;
import com.example.roadmap.infrastructure.adapter.in.web.dto.RoadmapResponse;

import java.util.List;
import java.util.stream.Collectors;

public final class RoadmapWebMapper {
    private RoadmapWebMapper() {}

    public static RoadmapResponse toResponse(Roadmap roadmap) {
        return new RoadmapResponse(
                roadmap.getId(),
                roadmap.getTitle(),
                roadmap.getDescription(),
                roadmap.getCreatedAt()
        );
    }

    public static List<RoadmapResponse> toResponseList(List<Roadmap> roadmaps) {
        return roadmaps.stream().map(RoadmapWebMapper::toResponse).collect(Collectors.toList());
    }
}
