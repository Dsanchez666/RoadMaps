package com.example.roadmap.infrastructure.adapter.out.persistence;

import com.example.roadmap.domain.model.Roadmap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileRoadmapRepositoryTest {

    @TempDir
    Path tempDir;

    @Test
    void saveAndRead() {
        Path file = tempDir.resolve("roadmaps.txt");
        FileRoadmapRepository repository = new FileRoadmapRepository(file);

        Roadmap created = new Roadmap("Title", "Desc");
        repository.save(created);

        Optional<Roadmap> found = repository.findById(created.getId());
        assertTrue(found.isPresent());
        assertEquals(created.getId(), found.get().getId());

        List<Roadmap> all = repository.findAll();
        assertEquals(1, all.size());
    }
}
