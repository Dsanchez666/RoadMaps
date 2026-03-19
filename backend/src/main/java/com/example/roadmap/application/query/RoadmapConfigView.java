package com.example.roadmap.application.query;

import java.util.List;

public record RoadmapConfigView(
        String id,
        String producto,
        String organizacion,
        HorizonView horizonte_base,
        List<AxisView> ejes_estrategicos,
        List<InitiativeView> iniciativas
) {}
