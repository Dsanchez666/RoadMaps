package com.example.roadmap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Roadmap backend application.
 *
 * This class boots the Spring context and initializes all configured adapters
 * and REST endpoints.
 *
 * @since 1.0
 */
@SpringBootApplication
public class Application {

    /**
     * Starts the Spring Boot application.
     *
     * @param args command-line startup arguments.
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}