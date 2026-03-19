package com.example.roadmap.infrastructure.adapter.out.persistence;

import com.example.roadmap.domain.model.Roadmap;
import com.example.roadmap.domain.port.out.RoadmapRepositoryPort;
import com.example.roadmap.infrastructure.db.DatabaseConnection;

import java.util.List;
import java.util.Optional;

public class RoutingRoadmapRepository implements RoadmapRepositoryPort {
    private final RoadmapRepositoryPort fileRepository;
    private final RoadmapRepositoryPort jdbcRepository;

    public RoutingRoadmapRepository(RoadmapRepositoryPort fileRepository, RoadmapRepositoryPort jdbcRepository) {
        this.fileRepository = fileRepository;
        this.jdbcRepository = jdbcRepository;
    }

    @Override
    public Roadmap save(Roadmap roadmap) {
        return selectRepository().save(roadmap);
    }

    @Override
    public Optional<Roadmap> findById(String id) {
        return selectRepository().findById(id);
    }

    @Override
    public List<Roadmap> findAll() {
        return selectRepository().findAll();
    }

    private RoadmapRepositoryPort selectRepository() {
        if ("MYSQL".equalsIgnoreCase(DatabaseConnection.getDatabaseType())) {
            return jdbcRepository;
        }
        return fileRepository;
    }
}
