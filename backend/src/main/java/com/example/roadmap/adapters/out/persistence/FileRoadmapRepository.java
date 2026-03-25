package com.example.roadmap.adapters.out.persistence;

import com.example.roadmap.domain.Roadmap;
import com.example.roadmap.domain.RoadmapRepository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * File-based implementation of {@link RoadmapRepository}.
 *
 * Data is stored in a plain text file using one roadmap per line. Fields are
 * pipe-separated and escaped to support pipe/backslash inside values.
 *
 * @since 1.0
 */
public class FileRoadmapRepository implements RoadmapRepository {
    private final Path file;
    private final Object lock = new Object();

    /**
     * Initializes file storage and creates directories/file when missing.
     */
    public FileRoadmapRepository() {
        this.file = Paths.get("..", "..", "data", "roadmaps.txt").toAbsolutePath().normalize();
        try {
            Files.createDirectories(file.getParent());
            if (!Files.exists(file)) Files.createFile(file);
        } catch (IOException e) {
            throw new RuntimeException("Unable to initialize data file: " + file, e);
        }
    }

    /**
     * Persists a roadmap by appending one encoded line to the file.
     *
     * @param roadmap Roadmap to store.
     * @return Stored roadmap.
     */
    @Override
    public Roadmap save(Roadmap roadmap) {
        String line = encode(roadmap.getId()) + "|" + encode(roadmap.getTitle()) + "|" + encode(roadmap.getDescription()) + "|" + roadmap.getCreatedAt().toString();
        synchronized (lock) {
            try {
                Files.write(file, (line + System.lineSeparator()).getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return roadmap;
    }

    /**
     * Returns the first roadmap matching the provided id.
     *
     * @param id Roadmap identifier.
     * @return Optional roadmap.
     */
    @Override
    public Optional<Roadmap> findById(String id) {
        synchronized (lock) {
            List<Roadmap> all = readAll();
            return all.stream().filter(r -> r.getId().equals(id)).findFirst();
        }
    }

    /**
     * Returns all roadmaps from file storage.
     *
     * @return List of roadmaps.
     */
    @Override
    public List<Roadmap> findAll() {
        synchronized (lock) {
            return readAll();
        }
    }

    /**
     * Reads and decodes all lines from the storage file.
     *
     * Additional Details:
     * Parsing is escape-aware; unescaped separators split columns while escaped
     * separators are preserved inside values.
     *
     * @return List of deserialized roadmaps.
     */
    private List<Roadmap> readAll() {
        List<Roadmap> out = new ArrayList<>();
        try {
            if (!Files.exists(file)) return out;
            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            for (String line : lines) {
                if (line.trim().isEmpty()) continue;
                String[] parts = parseLine(line);
                if (parts.length >= 4) {
                    String id = decode(parts[0]);
                    String title = decode(parts[1]);
                    String description = decode(parts[2]);
                    Instant createdAt = Instant.parse(parts[3]);
                    out.add(new Roadmap(id, title, description, createdAt));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return out;
    }

    /**
     * Escapes field values before persistence.
     *
     * @param s Raw field value.
     * @return Escaped value safe for pipe-separated serialization.
     */
    private String encode(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("|", "\\|");
    }

    /**
     * Restores escaped values after reading from storage.
     *
     * @param s Escaped value.
     * @return Unescaped value.
     */
    private String decode(String s) {
        if (s == null) return "";
        // The order matters: unescape backslash first, then pipe markers.
        String t = s.replace("\\\\", "\\");
        return t.replace("\\|", "|");
    }

    /**
     * Splits one serialized line into fields honoring escape sequences.
     *
     * @param line Serialized roadmap line.
     * @return Parsed fields.
     */
    private String[] parseLine(String line) {
        List<String> parts = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean esc = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (esc) {
                cur.append(c);
                esc = false;
            } else if (c == '\\') {
                esc = true;
            } else if (c == '|') {
                parts.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        parts.add(cur.toString());
        return parts.toArray(new String[0]);
    }
}