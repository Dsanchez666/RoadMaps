package com.example.roadmap.infrastructure.adapter.in.web;

import com.example.roadmap.application.query.RoadmapConfigQueryService;
import com.example.roadmap.application.query.RoadmapConfigView;
import com.example.roadmap.domain.model.Roadmap;
import com.example.roadmap.domain.port.in.RoadmapCommandPort;
import com.example.roadmap.domain.port.in.RoadmapQueryPort;
import com.example.roadmap.infrastructure.adapter.in.web.dto.RoadmapCreateRequest;
import com.example.roadmap.infrastructure.adapter.in.web.dto.RoadmapResponse;
import com.example.roadmap.infrastructure.adapter.in.web.mapper.RoadmapWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/roadmaps")
@Validated
public class RoadmapController {
    private final RoadmapCommandPort commandPort;
    private final RoadmapQueryPort queryPort;
    private final RoadmapConfigQueryService configQueryService;

    public RoadmapController(RoadmapCommandPort commandPort,
                             RoadmapQueryPort queryPort,
                             RoadmapConfigQueryService configQueryService) {
        this.commandPort = commandPort;
        this.queryPort = queryPort;
        this.configQueryService = configQueryService;
    }

    @PostMapping
    public ResponseEntity<RoadmapResponse> create(@Valid @RequestBody RoadmapCreateRequest dto) {
        Roadmap created = commandPort.create(dto.title(), dto.description());
        return ResponseEntity.created(URI.create("/api/roadmaps/" + created.getId()))
                .body(RoadmapWebMapper.toResponse(created));
    }

    @GetMapping
    public List<RoadmapResponse> list() {
        return RoadmapWebMapper.toResponseList(queryPort.list());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoadmapResponse> get(@PathVariable String id) {
        return queryPort.getById(id)
                .map(RoadmapWebMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/config")
    public ResponseEntity<RoadmapConfigView> getConfig(@PathVariable String id) {
        RoadmapConfigView cfg = configQueryService.getById(id);
        if (cfg == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(cfg);
    }
}
