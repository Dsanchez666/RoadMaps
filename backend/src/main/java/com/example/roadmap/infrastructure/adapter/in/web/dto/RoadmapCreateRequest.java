package com.example.roadmap.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record RoadmapCreateRequest(
        @NotBlank String title,
        String description
) {}
