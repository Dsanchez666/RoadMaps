package com.example.roadmap.infrastructure.adapter.in.web.dto;

import java.time.Instant;

public record RoadmapResponse(
        String id,
        String title,
        String description,
        Instant createdAt
) {}
