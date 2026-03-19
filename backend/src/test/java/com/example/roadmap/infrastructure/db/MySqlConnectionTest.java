package com.example.roadmap.infrastructure.db;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.sql.Connection;
import java.sql.DriverManager;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MySqlConnectionTest {

    @Test
    @EnabledIfEnvironmentVariable(named = "MYSQL_TEST", matches = "true")
    void connect_with_roadmap_user() throws Exception {
        String url = "jdbc:mysql://localhost:3306/roadmap_mvp?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8";
        try (Connection conn = DriverManager.getConnection(url, "roadmap", "roadmap123")) {
            assertNotNull(conn);
            assertTrue(conn.isValid(2));
        }
    }
}
