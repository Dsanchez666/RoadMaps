package com.example.roadmap.infrastructure.adapter.in.web;

import com.example.roadmap.infrastructure.adapter.in.web.dto.DatabaseConnectResponse;
import com.example.roadmap.infrastructure.adapter.in.web.dto.DatabaseDisconnectResponse;
import com.example.roadmap.infrastructure.adapter.in.web.dto.DatabaseStatusResponse;
import com.example.roadmap.infrastructure.adapter.in.web.dto.DatabaseSupportedTypesResponse;
import com.example.roadmap.infrastructure.adapter.in.web.dto.DatabaseTypeInfo;
import com.example.roadmap.infrastructure.db.DatabaseConnection;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;

@RestController
@RequestMapping("/api/database")
@Validated
public class DatabaseConfigController {

    @PostMapping("/connect/mysql")
    public ResponseEntity<DatabaseConnectResponse> connectMySQL(
            @RequestParam(defaultValue = "localhost") @NotBlank String host,
            @RequestParam(defaultValue = "3306") @NotBlank String port,
            @RequestParam(defaultValue = "roadmap_mvp") @NotBlank String database,
            @RequestParam(defaultValue = "root") @NotBlank String user,
            @RequestParam(defaultValue = "") String password) {

        try {
            DatabaseConnection.setMySQLParams(host, port, database, user, password);
            DatabaseConnection.setDatabaseType("MYSQL");

            Connection conn = DatabaseConnection.connect();

            if (conn != null && DatabaseConnection.isConnected()) {
                return ResponseEntity.ok(new DatabaseConnectResponse(
                        "SUCCESS",
                        "Successfully connected to MySQL database",
                        "MYSQL",
                        DatabaseConnection.getConnectionInfo(),
                        host,
                        port,
                        database,
                        null
                ));
            }
            return ResponseEntity.status(500).body(new DatabaseConnectResponse(
                    "FAILED",
                    "Connection returned null - check driver availability",
                    "MYSQL",
                    null,
                    host,
                    port,
                    database,
                    null
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new DatabaseConnectResponse(
                    "ERROR",
                    e.getMessage(),
                    "MYSQL",
                    null,
                    host,
                    port,
                    database,
                    null
            ));
        }
    }

    @PostMapping("/connect/oracle")
    public ResponseEntity<DatabaseConnectResponse> connectOracle(
            @RequestParam @NotBlank String host,
            @RequestParam(defaultValue = "1521") @NotBlank String port,
            @RequestParam @NotBlank String sid,
            @RequestParam @NotBlank String user,
            @RequestParam @NotBlank String password) {

        try {
            DatabaseConnection.setOracleParams(host, port, sid, user, password);
            DatabaseConnection.setDatabaseType("ORACLE");

            Connection conn = DatabaseConnection.connect();

            if (conn != null && DatabaseConnection.isConnected()) {
                return ResponseEntity.ok(new DatabaseConnectResponse(
                        "SUCCESS",
                        "Successfully connected to Oracle database",
                        "ORACLE",
                        DatabaseConnection.getConnectionInfo(),
                        host,
                        port,
                        null,
                        sid
                ));
            }
            return ResponseEntity.status(500).body(new DatabaseConnectResponse(
                    "FAILED",
                    "Connection returned null - check driver availability and credentials",
                    "ORACLE",
                    null,
                    host,
                    port,
                    null,
                    sid
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new DatabaseConnectResponse(
                    "ERROR",
                    e.getMessage(),
                    "ORACLE",
                    null,
                    host,
                    port,
                    null,
                    sid
            ));
        }
    }

    @GetMapping("/status")
    public ResponseEntity<DatabaseStatusResponse> getStatus() {
        return ResponseEntity.ok(new DatabaseStatusResponse(
                DatabaseConnection.isConnected(),
                DatabaseConnection.getDatabaseType(),
                DatabaseConnection.getConnectionInfo()
        ));
    }

    @PostMapping("/disconnect")
    public ResponseEntity<DatabaseDisconnectResponse> disconnect() {
        try {
            DatabaseConnection.disconnect();
            return ResponseEntity.ok(new DatabaseDisconnectResponse(
                    "SUCCESS",
                    "Successfully disconnected from database",
                    false
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new DatabaseDisconnectResponse(
                    "ERROR",
                    e.getMessage(),
                    DatabaseConnection.isConnected()
            ));
        }
    }

    @GetMapping("/supported-types")
    public ResponseEntity<DatabaseSupportedTypesResponse> getSupportedTypes() {
        DatabaseTypeInfo mysqlInfo = new DatabaseTypeInfo(
                "MySQL",
                "3306",
                new String[]{"host", "port", "database", "user", "password"}
        );
        DatabaseTypeInfo oracleInfo = new DatabaseTypeInfo(
                "Oracle",
                "1521",
                new String[]{"host", "port", "sid", "user", "password"}
        );

        return ResponseEntity.ok(new DatabaseSupportedTypesResponse(
                new String[]{"MYSQL", "ORACLE"},
                "The application supports both MySQL and Oracle databases",
                mysqlInfo,
                oracleInfo
        ));
    }
}
