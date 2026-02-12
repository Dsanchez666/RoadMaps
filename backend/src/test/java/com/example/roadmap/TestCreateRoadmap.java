package com.example.roadmap;

import com.example.roadmap.adapters.out.persistence.FileRoadmapRepository;
import com.example.roadmap.application.CreateRoadmapUseCase;
import com.example.roadmap.domain.Roadmap;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class TestCreateRoadmap {
    public static void main(String[] args) throws Exception {
        // Ensure clean state: remove data file if exists
        Path dataFile = Path.of("..", "..", "data", "roadmaps.txt").toAbsolutePath().normalize();
        try { Files.deleteIfExists(dataFile); } catch (Exception ignored) {}

        FileRoadmapRepository repo = new FileRoadmapRepository();
        CreateRoadmapUseCase useCase = new CreateRoadmapUseCase(repo);

        int before = repo.findAll().size();
        Roadmap r = useCase.create("TestFromUnit","Desc");
        List<Roadmap> all = repo.findAll();
        int after = all.size();

        System.out.println("Created id: " + r.getId());
        System.out.println("Before: " + before + " After: " + after);

        if (after == before + 1) {
            System.out.println("TEST PASSED");
            System.exit(0);
        } else {
            System.out.println("TEST FAILED");
            System.exit(1);
        }
    }
}
