package com.example.roadmap.infrastructure.adapter.in.web.dto;

public record DatabaseTypeInfo(
        String name,
        String defaultPort,
        String[] requiredParams
) {}
