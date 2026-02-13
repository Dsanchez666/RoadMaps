package com.example.roadmap;

import java.sql.*;

/**
 * Example usage of DatabaseConnection supporting MySQL and Oracle
 */
public class DatabaseConnectionExample {
    
    /**
     * Example 1: Basic MySQL Connection using default parameters
     */
    public static void connectToMySQLDefault() throws Exception {
        System.out.println("\n=== Example 1: Connect to MySQL (defaults) ===");
        
        // Using default MySQL parameters
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
     * Example 2: MySQL Connection with custom parameters
     */
    public static void connectToMySQLCustom() throws Exception {
        System.out.println("\n=== Example 2: Connect to MySQL (custom parameters) ===");
        
        // Set custom MySQL connection parameters
        MySQLConnection.setConnectionParams(
            "localhost",      // host
            "3306",           // port
            "roadmap_mvp",    // database
            "root",           // username
            "mypassword"      // password
        );
        
        Connection conn = MySQLConnection.connect();
        
        if (MySQLConnection.isConnected()) {
            System.out.println("✓ Connected to MySQL");
            System.out.println("  Connection Info: " + MySQLConnection.getConnectionInfo());
            
            // Example: Execute a simple query
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
     * Example 3: Oracle Connection with SID
     */
    public static void connectToOracleSID() throws Exception {
        System.out.println("\n=== Example 3: Connect to Oracle (with SID) ===");
        
        // Connect to Oracle database
        Connection conn = MySQLConnection.connectToOracle(
            "oracle-server.example.com",  // host
            "1521",                        // port (Oracle default)
            "ORCL",                        // SID
            "system",                      // username
            "oracle"                       // password
        );
        
        if (MySQLConnection.isConnected()) {
            System.out.println("✓ Connected to Oracle");
            System.out.println("  Connection Info: " + MySQLConnection.getConnectionInfo());
            
            // Example: Query Oracle
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
     * Example 4: Using DatabaseConnection class directly for more control
     */
    public static void connectUsingDatabaseConnection() throws Exception {
        System.out.println("\n=== Example 4: Using DatabaseConnection class ===");
        
        // Configure for Oracle
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
     * Example 5: Switch between databases at runtime
     */
    public static void switchDatabasesAtRuntime() throws Exception {
        System.out.println("\n=== Example 5: Switch between databases ===");
        
        // Start with MySQL
        System.out.println("\nConnecting to MySQL...");
        DatabaseConnection.setMySQLParams("localhost", "3306", "roadmap_mvp", "root", "");
        DatabaseConnection.setDatabaseType("MYSQL");
        
        Connection mysqlConn = DatabaseConnection.connect();
        if (DatabaseConnection.isConnected()) {
            System.out.println("✓ Connected to MySQL: " + DatabaseConnection.getConnectionInfo());
            // Do some work with MySQL
            DatabaseConnection.disconnect();
        }
        
        // Switch to Oracle
        System.out.println("\nSwitching to Oracle...");
        DatabaseConnection.setOracleParams("oracle-server", "1521", "ORCL", "system", "oracle");
        DatabaseConnection.setDatabaseType("ORACLE");
        
        Connection oracleConn = DatabaseConnection.connect();
        if (DatabaseConnection.isConnected()) {
            System.out.println("✓ Connected to Oracle: " + DatabaseConnection.getConnectionInfo());
            // Do some work with Oracle
            DatabaseConnection.disconnect();
        }
    }

    /**
     * Example 6: With proper error handling
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
                // Perform database operations
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
     * Main method to run all examples
     */
    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║     Database Connection Examples - MySQL & Oracle          ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        
        try {
            // Uncomment to run individual examples:
            
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
