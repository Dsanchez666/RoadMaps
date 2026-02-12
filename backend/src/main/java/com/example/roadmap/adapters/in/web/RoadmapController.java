package com.example.roadmap.adapters.in.web;

import com.example.roadmap.application.CreateRoadmapUseCase;
import com.example.roadmap.domain.Roadmap;
import com.example.roadmap.adapters.out.persistence.InMemoryRoadmapRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/roadmaps")
public class RoadmapController {
    private final CreateRoadmapUseCase useCase;

    public RoadmapController() {
        // In a real app use Spring configuration to wire adapters
        this.useCase = new CreateRoadmapUseCase(new InMemoryRoadmapRepository());
    }

    @PostMapping
    public ResponseEntity<Roadmap> create(@RequestBody CreateDto dto) {
        Roadmap created = useCase.create(dto.title, dto.description);
        return ResponseEntity.created(URI.create("/api/roadmaps/" + created.getId())).body(created);
    }

    @GetMapping
    public List<Roadmap> list() {
        return useCase.list();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Roadmap> get(@PathVariable String id) {
        return useCase.getById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    public static class CreateDto {
        public String title;
        public String description;
    }
}
