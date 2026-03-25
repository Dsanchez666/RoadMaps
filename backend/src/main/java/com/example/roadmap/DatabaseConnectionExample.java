package com.example.roadmap;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Example executable scenarios for the database connection layer.
 *
 * This utility demonstrates MySQL and Oracle connectivity, runtime switching,
 * query execution and error handling flows.
 *
 * @since 1.0
 */
public class DatabaseConnectionExample {

    /**
     * Example 1: Basic MySQL connection using default parameters.
     *
     * @throws Exception If connection or SQL operation fails.
     */
    public static void connectToMySQLDefault() throws Exception {
        System.out.println("\n=== Example 1: Connect to MySQL (defaults) ===");

        Connection conn = MySQLConnection.connect();

        if (MySQLConnection.isConnected()) {
            System.out.println("✓ Connected to MySQL");
            System.out.println("  Connection Info: " + MySQLConnection.getConnectionInfo());
            MySQLConnection.disconnect();
        } else {
            System.out.println("✗ Failed to connect to MySQL");
        }
    }

    /**
     * Example 2: MySQL connection with custom parameters.
     *
     * @throws Exception If connection or SQL operation fails.
     */
    public static void connectToMySQLCustom() throws Exception {
        System.out.println("\n=== Example 2: Connect to MySQL (custom parameters) ===");

        MySQLConnection.setConnectionParams(
            "localhost",
            "3306",
            "roadmap_mvp",
            "root",
            "mypassword"
        );

        Connection conn = MySQLConnection.connect();

        if (MySQLConnection.isConnected()) {
            System.out.println("✓ Connected to MySQL");
            System.out.println("  Connection Info: " + MySQLConnection.getConnectionInfo());

            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT 1 as test");
                if (rs.next()) {
                    System.out.println("  Query test result: " + rs.getInt("test"));
                }
            }

            MySQLConnection.disconnect();
        } else {
            System.out.println("✗ Failed to connect to MySQL");
        }
    }

    /**
     * Example 3: Oracle connection with SID format.
     *
     * @throws Exception If connection or SQL operation fails.
     */
    public static void connectToOracleSID() throws Exception {
        System.out.println("\n=== Example 3: Connect to Oracle (with SID) ===");

        Connection conn = MySQLConnection.connectToOracle(
            "oracle-server.example.com",
            "1521",
            "ORCL",
            "system",
            "oracle"
        );

        if (MySQLConnection.isConnected()) {
            System.out.println("✓ Connected to Oracle");
            System.out.println("  Connection Info: " + MySQLConnection.getConnectionInfo());

            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT 1 as test FROM dual");
                if (rs.next()) {
                    System.out.println("  Query test result: " + rs.getInt("test"));
                }
            }

            MySQLConnection.disconnect();
        } else {
            System.out.println("✗ Failed to connect to Oracle");
        }
    }

    /**
     * Example 4: Direct usage of DatabaseConnection API.
     *
     * @throws Exception If connection fails.
     */
    public static void connectUsingDatabaseConnection() throws Exception {
        System.out.println("\n=== Example 4: Using DatabaseConnection class ===");

        DatabaseConnection.setOracleParams(
            "oracle-server.example.com",
            "1521",
            "ORCL",
            "system",
            "oracle"
        );
        DatabaseConnection.setDatabaseType("ORACLE");

        Connection conn = DatabaseConnection.connect();

        if (DatabaseConnection.isConnected()) {
            System.out.println("✓ Connected via DatabaseConnection");
            System.out.println("  Type: " + DatabaseConnection.getDatabaseType());
            System.out.println("  Connection Info: " + DatabaseConnection.getConnectionInfo());
            DatabaseConnection.disconnect();
        }
    }

    /**
     * Example 5: Runtime switch between MySQL and Oracle.
     *
     * @throws Exception If any connection attempt fails.
     */
    public static void switchDatabasesAtRuntime() throws Exception {
        System.out.println("\n=== Example 5: Switch between databases ===");

        System.out.println("\nConnecting to MySQL...");
        DatabaseConnection.setMySQLParams("localhost", "3306", "roadmap_mvp", "root", "");
        DatabaseConnection.setDatabaseType("MYSQL");

        Connection mysqlConn = DatabaseConnection.connect();
        if (DatabaseConnection.isConnected()) {
            System.out.println("✓ Connected to MySQL: " + DatabaseConnection.getConnectionInfo());
            DatabaseConnection.disconnect();
        }

        System.out.println("\nSwitching to Oracle...");
        DatabaseConnection.setOracleParams("oracle-server", "1521", "ORCL", "system", "oracle");
        DatabaseConnection.setDatabaseType("ORACLE");

        Connection oracleConn = DatabaseConnection.connect();
        if (DatabaseConnection.isConnected()) {
            System.out.println("✓ Connected to Oracle: " + DatabaseConnection.getConnectionInfo());
            DatabaseConnection.disconnect();
        }
    }

    /**
     * Example 6: Defensive error handling around connection attempts.
     */
    public static void connectWithErrorHandling() {
        System.out.println("\n=== Example 6: Error handling ===");

        try {
            System.out.println("Attempting to connect to Oracle...");

            DatabaseConnection.setOracleParams(
                "invalid-host",
                "1521",
                "ORCL",
                "system",
                "oracle"
            );
            DatabaseConnection.setDatabaseType("ORACLE");

            Connection conn = DatabaseConnection.connect();

            if (conn != null && DatabaseConnection.isConnected()) {
                System.out.println("✓ Connected successfully!");
                DatabaseConnection.disconnect();
            } else {
                System.out.println("✗ Connection returned null - check driver availability");
            }
        } catch (Exception e) {
            System.err.println("✗ Exception occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Runs selected examples.
     *
     * @param args Command-line arguments.
     */
    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║     Database Connection Examples - MySQL & Oracle          ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");

        try {
            // connectToMySQLDefault();
            // connectToMySQLCustom();
            // connectToOracleSID();
            connectUsingDatabaseConnection();
            // switchDatabasesAtRuntime();
            connectWithErrorHandling();

        } catch (Exception e) {
            System.err.println("Error in examples: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("║                    Examples completed                       ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
    }
}