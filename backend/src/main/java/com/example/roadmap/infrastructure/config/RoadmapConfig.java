package com.example.roadmap.infrastructure.config;

import com.example.roadmap.application.usecase.RoadmapUseCaseService;
import com.example.roadmap.domain.port.out.RoadmapRepositoryPort;
import com.example.roadmap.infrastructure.adapter.out.persistence.FileRoadmapRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;

@Configuration
public class RoadmapConfig {

    @Bean
    public RoadmapRepositoryPort roadmapRepository(
            @Value("${roadmap.data.path:data/roadmaps.txt}") String dataPath) {
        return new FileRoadmapRepository(Path.of(dataPath));
    }

    @Bean
    public RoadmapUseCaseService roadmapUseCaseService(RoadmapRepositoryPort repository) {
        return new RoadmapUseCaseService(repository);
    }
}
