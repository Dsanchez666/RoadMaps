package com.example.roadmap.infrastructure.adapter.in.web.dto;

public record DatabaseSupportedTypesResponse(
        String[] types,
        String description,
        DatabaseTypeInfo mysql,
        DatabaseTypeInfo oracle
) {}
