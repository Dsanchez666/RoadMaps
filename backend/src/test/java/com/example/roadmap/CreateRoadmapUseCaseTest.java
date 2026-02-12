package com.example.roadmap;

import com.example.roadmap.adapters.out.persistence.InMemoryRoadmapRepository;
import com.example.roadmap.application.CreateRoadmapUseCase;
import com.example.roadmap.domain.Roadmap;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CreateRoadmapUseCaseTest {

    @Test
    public void createRoadmapSavesAndReturns() {
        InMemoryRoadmapRepository repo = new InMemoryRoadmapRepository();
        CreateRoadmapUseCase useCase = new CreateRoadmapUseCase(repo);

        Roadmap r = useCase.create("Test","Desc");

        assertNotNull(r.getId());
        assertEquals("Test", r.getTitle());
        assertEquals(1, repo.findAll().size());
    }
}
