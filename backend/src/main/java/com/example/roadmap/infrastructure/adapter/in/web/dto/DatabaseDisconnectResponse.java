package com.example.roadmap.infrastructure.adapter.in.web.dto;

public record DatabaseDisconnectResponse(
        String status,
        String message,
        boolean connected
) {}
