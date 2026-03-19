package com.example.roadmap.infrastructure.adapter.out.persistence;

import com.example.roadmap.domain.model.Roadmap;
import com.example.roadmap.domain.port.out.RoadmapRepositoryPort;
import com.example.roadmap.infrastructure.db.RoadmapJdbcGateway;

import java.util.List;
import java.util.Optional;

public class JdbcRoadmapRepository implements RoadmapRepositoryPort {
    private final RoadmapJdbcGateway gateway;

    public JdbcRoadmapRepository(RoadmapJdbcGateway gateway) {
        this.gateway = gateway;
    }

    @Override
    public Roadmap save(Roadmap roadmap) {
        return gateway.save(roadmap);
    }

    @Override
    public Optional<Roadmap> findById(String id) {
        return gateway.findById(id);
    }

    @Override
    public List<Roadmap> findAll() {
        return gateway.findAll();
    }
}
