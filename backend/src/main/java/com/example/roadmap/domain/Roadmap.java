package com.example.roadmap.domain;

import java.time.Instant;
import java.util.UUID;

public class Roadmap {
    private final String id;
    private final String title;
    private final String description;
    private final Instant createdAt;

    public Roadmap(String title, String description) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.description = description;
        this.createdAt = Instant.now();
    }

    public Roadmap(String id, String title, String description, Instant createdAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Instant getCreatedAt() { return createdAt; }
}
