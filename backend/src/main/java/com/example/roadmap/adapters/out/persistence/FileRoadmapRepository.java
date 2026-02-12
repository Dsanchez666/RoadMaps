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

public class FileRoadmapRepository implements RoadmapRepository {
    private final Path file;
    private final Object lock = new Object();

    public FileRoadmapRepository() {
        this.file = Paths.get("..", "..", "data", "roadmaps.txt").toAbsolutePath().normalize();
        try {
            Files.createDirectories(file.getParent());
            if (!Files.exists(file)) Files.createFile(file);
        } catch (IOException e) {
            throw new RuntimeException("Unable to initialize data file: " + file, e);
        }
    }

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

    @Override
    public Optional<Roadmap> findById(String id) {
        synchronized (lock) {
            List<Roadmap> all = readAll();
            return all.stream().filter(r -> r.getId().equals(id)).findFirst();
        }
    }

    @Override
    public List<Roadmap> findAll() {
        synchronized (lock) {
            return readAll();
        }
    }

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

    // encode replaces backslash and pipe
    private String encode(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("|", "\\|");
    }

    private String decode(String s) {
        if (s == null) return "";
        // unescape: first replace escaped backslash, then escaped pipe
        String t = s.replace("\\\\", "\\");
        return t.replace("\\|", "|");
    }

    // parse taking into account escaped pipes
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
