package com.example.roadmap.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain entity representing a roadmap aggregate.
 *
 * A roadmap is created with an immutable identifier and timestamp. It is the
 * core business object exchanged between use cases and adapters.
 *
 * @since 1.0
 */
public class Roadmap {
    private final String id;
    private final String title;
    private final String description;
    private final Instant createdAt;

    /**
     * Creates a new roadmap with generated id and current timestamp.
     *
     * @param title Roadmap title shown in the UI.
     * @param description Additional roadmap description.
     */
    public Roadmap(String title, String description) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.description = description;
        this.createdAt = Instant.now();
    }

    /**
     * Rehydrates an existing roadmap from persistence data.
     *
     * @param id Unique roadmap identifier.
     * @param title Roadmap title.
     * @param description Roadmap description.
     * @param createdAt Creation timestamp.
     */
    public Roadmap(String id, String title, String description, Instant createdAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.createdAt = createdAt;
    }

    /** @return Unique roadmap id. */
    public String getId() { return id; }

    /** @return Roadmap title. */
    public String getTitle() { return title; }

    /** @return Roadmap description. */
    public String getDescription() { return description; }

    /** @return Creation timestamp in UTC. */
    public Instant getCreatedAt() { return createdAt; }
}