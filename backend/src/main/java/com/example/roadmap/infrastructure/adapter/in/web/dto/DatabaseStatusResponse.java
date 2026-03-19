package com.example.roadmap.infrastructure.adapter.in.web.dto;

public record DatabaseStatusResponse(
        boolean connected,
        String type,
        String connectionUrl
) {}
