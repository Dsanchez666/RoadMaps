package com.example.roadmap;

import java.sql.*;

/**
 * MySQL Connection wrapper for backward compatibility.
 * This class delegates to DatabaseConnection for flexibility.
 * Supports both MySQL and Oracle databases.
 */
public class MySQLConnection {

    /**
     * Set MySQL connection parameters
     */
    public static void setConnectionParams(String host, String port, String database, String user, String password) {
        DatabaseConnection.setMySQLParams(host, port, database, user, password);
        DatabaseConnection.setDatabaseType("MYSQL");
    }

    /**
     * Connect to MySQL (default for backward compatibility)
     */
    public static Connection connect() throws Exception {
        DatabaseConnection.setDatabaseType("MYSQL");
        return DatabaseConnection.connect();
    }

    /**
     * Connect to Oracle database instead of MySQL
     * @param host Oracle server host
     * @param port Oracle server port (default 1521)
     * @param sid Oracle SID (e.g., ORCL)
     * @param user Oracle username
     * @param password Oracle password
     */
    public static Connection connectToOracle(String host, String port, String sid, String user, String password) throws Exception {
        DatabaseConnection.setOracleParams(host, port, sid, user, password);
        DatabaseConnection.setDatabaseType("ORACLE");
        return DatabaseConnection.connect();
    }

    // ==================== Delegation Methods ====================
    
    public static Connection getConnection() {
        return DatabaseConnection.getConnection();
    }

    public static void disconnect() {
        DatabaseConnection.disconnect();
    }

    public static boolean isConnected() {
        return DatabaseConnection.isConnected();
    }

    public static String getConnectionInfo() {
        return DatabaseConnection.getConnectionInfo();
    }
}
