package com.example.roadmap.adapters.in.web;

import com.example.roadmap.DatabaseConnection;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller to manage database connections dynamically.
 * Allows switching between MySQL and Oracle database at runtime.
 */
@RestController
@RequestMapping("/api/database")
public class DatabaseConfigController {

    /**
     * Connect to MySQL database
     * 
     * Example:
     * POST /api/database/connect/mysql
     * {
     *     "host": "localhost",
     *     "port": "3306",
     *     "database": "roadmap_mvp",
     *     "user": "root",
     *     "password": ""
     * }
     */
    @PostMapping("/connect/mysql")
    public ResponseEntity<Map<String, Object>> connectMySQL(
            @RequestParam(defaultValue = "localhost") String host,
            @RequestParam(defaultValue = "3306") String port,
            @RequestParam(defaultValue = "roadmap_mvp") String database,
            @RequestParam(defaultValue = "root") String user,
            @RequestParam(defaultValue = "") String password) {
        
        try {
            DatabaseConnection.setMySQLParams(host, port, database, user, password);
            DatabaseConnection.setDatabaseType("MYSQL");
            
            Connection conn = DatabaseConnection.connect();
            
            Map<String, Object> response = new HashMap<>();
            if (conn != null && DatabaseConnection.isConnected()) {
                response.put("status", "SUCCESS");
                response.put("message", "Successfully connected to MySQL database");
                response.put("type", "MYSQL");
                response.put("connectionUrl", DatabaseConnection.getConnectionInfo());
                response.put("host", host);
                response.put("port", port);
                response.put("database", database);
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "FAILED");
                response.put("message", "Connection returned null - check driver availability");
                response.put("type", "MYSQL");
                return ResponseEntity.status(500).body(response);
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            response.put("type", "MYSQL");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Connect to Oracle database
     * 
     * Example:
     * POST /api/database/connect/oracle
     * {
     *     "host": "oracle-server.company.com",
     *     "port": "1521",
     *     "sid": "ORCL",
     *     "user": "system",
     *     "password": "oracle"
     * }
     */
    @PostMapping("/connect/oracle")
    public ResponseEntity<Map<String, Object>> connectOracle(
            @RequestParam String host,
            @RequestParam(defaultValue = "1521") String port,
            @RequestParam String sid,
            @RequestParam String user,
            @RequestParam String password) {
        
        try {
            DatabaseConnection.setOracleParams(host, port, sid, user, password);
            DatabaseConnection.setDatabaseType("ORACLE");
            
            Connection conn = DatabaseConnection.connect();
            
            Map<String, Object> response = new HashMap<>();
            if (conn != null && DatabaseConnection.isConnected()) {
                response.put("status", "SUCCESS");
                response.put("message", "Successfully connected to Oracle database");
                response.put("type", "ORACLE");
                response.put("connectionUrl", DatabaseConnection.getConnectionInfo());
                response.put("host", host);
                response.put("port", port);
                response.put("sid", sid);
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "FAILED");
                response.put("message", "Connection returned null - check driver availability and credentials");
                response.put("type", "ORACLE");
                return ResponseEntity.status(500).body(response);
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            response.put("type", "ORACLE");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Get current connection status
     * 
     * Example:
     * GET /api/database/status
     * 
     * Response:
     * {
     *     "connected": true,
     *     "type": "ORACLE",
     *     "connectionUrl": "jdbc:oracle:thin:@oracle-server:1521:ORCL"
     * }
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("connected", DatabaseConnection.isConnected());
        response.put("type", DatabaseConnection.getDatabaseType());
        response.put("connectionUrl", DatabaseConnection.getConnectionInfo());
        return ResponseEntity.ok(response);
    }

    /**
     * Disconnect from current database
     * 
     * Example:
     * POST /api/database/disconnect
     */
    @PostMapping("/disconnect")
    public ResponseEntity<Map<String, Object>> disconnect() {
        try {
            DatabaseConnection.disconnect();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("message", "Successfully disconnected from database");
            response.put("connected", false);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Get supported database types
     * 
     * Example:
     * GET /api/database/supported-types
     */
    @GetMapping("/supported-types")
    public ResponseEntity<Map<String, Object>> getSupportedTypes() {
        Map<String, Object> response = new HashMap<>();
        response.put("types", new String[]{"MYSQL", "ORACLE"});
        response.put("description", "The application supports both MySQL and Oracle databases");
        
        Map<String, Object> mysqlInfo = new HashMap<>();
        mysqlInfo.put("name", "MySQL");
        mysqlInfo.put("defaultPort", "3306");
        mysqlInfo.put("requiredParams", new String[]{"host", "port", "database", "user", "password"});
        
        Map<String, Object> oracleInfo = new HashMap<>();
        oracleInfo.put("name", "Oracle");
        oracleInfo.put("defaultPort", "1521");
        oracleInfo.put("requiredParams", new String[]{"host", "port", "sid", "user", "password"});
        
        response.put("mysql", mysqlInfo);
        response.put("oracle", oracleInfo);
        return ResponseEntity.ok(response);
    }
}
