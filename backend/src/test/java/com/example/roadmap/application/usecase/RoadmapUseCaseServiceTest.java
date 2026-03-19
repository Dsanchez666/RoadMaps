package com.example.roadmap.application.usecase;

import com.example.roadmap.domain.model.Roadmap;
import com.example.roadmap.domain.port.out.RoadmapRepositoryPort;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoadmapUseCaseServiceTest {

    @Test
    void createAndQueryRoadmap() {
        RoadmapRepositoryPort repository = new InMemoryStubRepository();
        RoadmapUseCaseService service = new RoadmapUseCaseService(repository);

        Roadmap created = service.create("Title", "Desc");

        assertNotNull(created.getId());
        assertEquals("Title", created.getTitle());

        Optional<Roadmap> fetched = service.getById(created.getId());
        assertTrue(fetched.isPresent());
        assertEquals(created.getId(), fetched.get().getId());

        List<Roadmap> all = service.list();
        assertEquals(1, all.size());
    }

    private static final class InMemoryStubRepository implements RoadmapRepositoryPort {
        private final ConcurrentHashMap<String, Roadmap> store = new ConcurrentHashMap<>();

        @Override
        public Roadmap save(Roadmap roadmap) {
            store.put(roadmap.getId(), roadmap);
            return roadmap;
        }

        @Override
        public Optional<Roadmap> findById(String id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public List<Roadmap> findAll() {
            return new ArrayList<>(store.values());
        }
    }
}
