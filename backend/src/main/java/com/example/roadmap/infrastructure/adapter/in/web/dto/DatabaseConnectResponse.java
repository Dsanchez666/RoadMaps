package com.example.roadmap.infrastructure.adapter.in.web.dto;

public record DatabaseConnectResponse(
        String status,
        String message,
        String type,
        String connectionUrl,
        String host,
        String port,
        String database,
        String sid
) {}
