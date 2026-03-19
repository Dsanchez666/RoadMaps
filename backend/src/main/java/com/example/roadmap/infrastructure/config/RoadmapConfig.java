package com.example.roadmap.infrastructure.config;

import com.example.roadmap.application.query.RoadmapConfigQueryService;
import com.example.roadmap.application.query.RoadmapConfigReader;
import com.example.roadmap.application.usecase.RoadmapUseCaseService;
import com.example.roadmap.domain.port.out.RoadmapRepositoryPort;
import com.example.roadmap.infrastructure.adapter.out.persistence.FileRoadmapRepository;
import com.example.roadmap.infrastructure.adapter.out.persistence.JdbcRoadmapRepository;
import com.example.roadmap.infrastructure.adapter.out.persistence.RoutingRoadmapRepository;
import com.example.roadmap.infrastructure.db.RoadmapConfigJdbcReader;
import com.example.roadmap.infrastructure.db.RoadmapJdbcGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.nio.file.Path;

@Configuration
public class RoadmapConfig {

    @Bean
    public RoadmapRepositoryPort fileRoadmapRepository(
            @Value("${roadmap.data.path:data/roadmaps.txt}") String dataPath) {
        return new FileRoadmapRepository(Path.of(dataPath));
    }

    @Bean
    public RoadmapJdbcGateway roadmapJdbcGateway() {
        return new RoadmapJdbcGateway();
    }

    @Bean
    public RoadmapRepositoryPort jdbcRoadmapRepository(RoadmapJdbcGateway gateway) {
        return new JdbcRoadmapRepository(gateway);
    }

    @Bean
    @Primary
    public RoadmapRepositoryPort roadmapRepository(
            RoadmapRepositoryPort fileRoadmapRepository,
            RoadmapRepositoryPort jdbcRoadmapRepository) {
        return new RoutingRoadmapRepository(fileRoadmapRepository, jdbcRoadmapRepository);
    }

    @Bean
    public RoadmapUseCaseService roadmapUseCaseService(RoadmapRepositoryPort repository) {
        return new RoadmapUseCaseService(repository);
    }

    @Bean
    public RoadmapConfigReader roadmapConfigReader() {
        return new RoadmapConfigJdbcReader();
    }

    @Bean
    public RoadmapConfigQueryService roadmapConfigQueryService(RoadmapConfigReader reader) {
        return new RoadmapConfigQueryService(reader);
    }
}
