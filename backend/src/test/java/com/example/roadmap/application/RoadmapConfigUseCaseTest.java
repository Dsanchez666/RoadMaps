package com.example.roadmap.application;

import com.example.roadmap.domain.Roadmap;
import com.example.roadmap.domain.RoadmapConfig;
import com.example.roadmap.domain.RoadmapConfigRepository;
import com.example.roadmap.domain.RoadmapRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoadmapConfigUseCaseTest {

    @Test
    void getByRoadmapIdReturnsSavedConfigWhenPresent() {
        InMemoryRoadmapRepositoryStub roadmapRepository = new InMemoryRoadmapRepositoryStub();
        InMemoryConfigRepositoryStub configRepository = new InMemoryConfigRepositoryStub();
        RoadmapConfig expected = new RoadmapConfig();
        expected.setProducto("ETNA");
        configRepository.saved = expected;

        RoadmapConfigUseCase useCase = new RoadmapConfigUseCase(roadmapRepository, configRepository);
        Optional<RoadmapConfig> result = useCase.getByRoadmapId("rm-1");

        assertTrue(result.isPresent());
        assertEquals("ETNA", result.get().getProducto());
    }

    @Test
    void getByRoadmapIdBuildsDefaultFromRoadmapWhenConfigMissing() {
        InMemoryRoadmapRepositoryStub roadmapRepository = new InMemoryRoadmapRepositoryStub();
        InMemoryConfigRepositoryStub configRepository = new InMemoryConfigRepositoryStub();
        roadmapRepository.roadmap = new Roadmap("rm-1", "Roadmap Base", "desc", Instant.now());

        RoadmapConfigUseCase useCase = new RoadmapConfigUseCase(roadmapRepository, configRepository);
        Optional<RoadmapConfig> result = useCase.getByRoadmapId("rm-1");

        assertTrue(result.isPresent());
        assertEquals("Roadmap Base", result.get().getProducto());
        assertNotNull(result.get().getHorizonte_base());
    }

    @Test
    void saveForRoadmapDelegatesToRepository() {
        InMemoryRoadmapRepositoryStub roadmapRepository = new InMemoryRoadmapRepositoryStub();
        InMemoryConfigRepositoryStub configRepository = new InMemoryConfigRepositoryStub();
        RoadmapConfigUseCase useCase = new RoadmapConfigUseCase(roadmapRepository, configRepository);
        RoadmapConfig config = new RoadmapConfig();
        config.setProducto("Test");

        useCase.saveForRoadmap("rm-2", config);

        assertEquals("rm-2", configRepository.lastRoadmapId);
        assertEquals("Test", configRepository.saved.getProducto());
    }

    private static class InMemoryRoadmapRepositoryStub implements RoadmapRepository {
        Roadmap roadmap;

        @Override
        public Roadmap save(Roadmap roadmap) {
            this.roadmap = roadmap;
            return roadmap;
        }

        @Override
        public Optional<Roadmap> findById(String id) {
            if (roadmap != null && roadmap.getId().equals(id)) {
                return Optional.of(roadmap);
            }
            return Optional.empty();
        }

        @Override
        public List<Roadmap> findAll() {
            if (roadmap == null) {
                return new ArrayList<>();
            }
            return List.of(roadmap);
        }
    }

    private static class InMemoryConfigRepositoryStub implements RoadmapConfigRepository {
        String lastRoadmapId;
        RoadmapConfig saved;

        @Override
        public Optional<RoadmapConfig> findByRoadmapId(String roadmapId) {
            return Optional.ofNullable(saved);
        }

        @Override
        public void saveForRoadmap(String roadmapId, RoadmapConfig config) {
            this.lastRoadmapId = roadmapId;
            this.saved = config;
        }
    }
}
