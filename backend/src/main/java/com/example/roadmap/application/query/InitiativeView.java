package com.example.roadmap.application.query;

import java.util.List;

public record InitiativeView(
        String id,
        String nombre,
        String eje,
        String tipo,
        String expediente,
        String inicio,
        String fin,
        String objetivo,
        String impacto_principal,
        String usuarios_afectados,
        List<DependencyView> dependencias,
        String certeza
) {}
