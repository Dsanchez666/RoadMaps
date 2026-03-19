package com.example.roadmap.application.query;

public class RoadmapConfigQueryService {
    private final RoadmapConfigReader reader;

    public RoadmapConfigQueryService(RoadmapConfigReader reader) {
        this.reader = reader;
    }

    public RoadmapConfigView getById(String id) {
        return reader.findById(id);
    }
}
