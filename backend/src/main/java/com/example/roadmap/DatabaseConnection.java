package com.example.roadmap;

import java.sql.*;
import java.util.Properties;

/**
 * Universal database connection manager supporting MySQL and Oracle
 * Cleanly separates database-specific configuration
 */
public class DatabaseConnection {
    
    // ==================== Enums ====================
    enum DatabaseType {
        MYSQL, ORACLE
    }
    
    // ==================== Configuration Class ====================
    /**
     * Encapsulates all connection parameters (common + specific)
     */
    static class Config {
        DatabaseType type;
        String host;
        String port;
        String user;
        String password;
        String database;  // MySQL-specific
        String sid;       // Oracle-specific
        
        Config(DatabaseType type) {
            this.type = type;
        }
        
        @Override
        public String toString() {
            return String.format("[%s] host=%s, port=%s, user=%s", 
                type, host, port, user);
        }
    }
    
    // ==================== Static State ====================
    private static Connection connection;
    private static DatabaseType currentType = DatabaseType.MYSQL;
    
    // Configuration for each database type
    private static final Config mysqlConfig = new Config(DatabaseType.MYSQL);
    private static final Config oracleConfig = new Config(DatabaseType.ORACLE);
    
    // Initialize defaults
    static {
        // MySQL defaults
        mysqlConfig.host = "localhost";
        mysqlConfig.port = "3306";
        mysqlConfig.database = "roadmap_mvp";
        mysqlConfig.user = "root";
        mysqlConfig.password = "";
        
        // Oracle defaults
        oracleConfig.host = "localhost";
        oracleConfig.port = "1521";
        oracleConfig.sid = "ORCL";
        oracleConfig.user = "system";
        oracleConfig.password = "oracle";
    }

    /**
     * Set MySQL connection parameters
     */
    public static void setMySQLParams(String host, String port, String database, String user, String password) {
        mysqlConfig.host = host;
        mysqlConfig.port = port;
        mysqlConfig.database = database;
        mysqlConfig.user = user;
        mysqlConfig.password = password;
    }

    /**
     * Set Oracle connection parameters
     * @param host Oracle server host
     * @param port Oracle server port (default 1521)
     * @param sid Oracle SID (e.g., ORCL)
     * @param user Oracle username
     * @param password Oracle password
     */
    public static void setOracleParams(String host, String port, String sid, String user, String password) {
        oracleConfig.host = host;
        oracleConfig.port = port;
        oracleConfig.sid = sid;
        oracleConfig.user = user;
        oracleConfig.password = password;
    }

    /**
     * Set database type (MYSQL or ORACLE)
     */
    public static void setDatabaseType(String type) {
        try {
            currentType = DatabaseType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            System.err.println("Warning: Invalid database type '" + type + "'. Using MYSQL.");
            currentType = DatabaseType.MYSQL;
        }
    }

    /**
     * Connect to the database based on configured type
     */
    public static Connection connect() throws Exception {
        Config config = getCurrentConfig();
        
        if (currentType == DatabaseType.ORACLE) {
            return connectToOracle(config);
        } else {
            return connectToMySQL(config);
        }
    }

    /**
     * Connect to MySQL database
     */
    private static Connection connectToMySQL(Config config) throws Exception {
        // Check if MySQL driver is available
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("MySQL JDBC driver not found. Using JSON storage only.");
            return null;
        }

        try {
            String url = "jdbc:mysql://" + config.host + ":" + config.port + "/" + config.database + 
                        "?useSSL=false&serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8";
            
            Properties props = new Properties();
            props.setProperty("user", config.user);
            props.setProperty("password", config.password);
            props.setProperty("useUnicode", "true");
            props.setProperty("characterEncoding", "UTF-8");

            connection = DriverManager.getConnection(url, props);
            System.out.println("Connected to MySQL: " + config);
            return connection;
        } catch (SQLException e) {
            System.err.println("MySQL connection failed: " + e.getMessage());
            return null;
        }
    }

    /**
     * Connect to Oracle database
     */
    private static Connection connectToOracle(Config config) throws Exception {
        // Check if Oracle driver is available
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException e) {
            System.out.println("Oracle JDBC driver not found. Using JSON storage only.");
            return null;
        }

        try {
            // Using SID connection format
            String url = "jdbc:oracle:thin:@" + config.host + ":" + config.port + ":" + config.sid;
            
            Properties props = new Properties();
            props.setProperty("user", config.user);
            props.setProperty("password", config.password);

            connection = DriverManager.getConnection(url, props);
            System.out.println("Connected to Oracle: " + config);
            return connection;
        } catch (SQLException e) {
            System.err.println("Oracle connection failed: " + e.getMessage());
            return null;
        }
    }

    // ==================== Helper Methods ====================
    
    /**
     * Get current configuration based on database type
     */
    private static Config getCurrentConfig() {
        return (currentType == DatabaseType.ORACLE) ? oracleConfig : mysqlConfig;
    }

    public static Connection getConnection() {
        return connection;
    }

    public static void disconnect() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                System.out.println("Disconnected from database.");
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }

    public static boolean isConnected() {
        if (connection == null) return false;
        try {
            return !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    public static String getConnectionInfo() {
        Config config = getCurrentConfig();
        if (currentType == DatabaseType.ORACLE) {
            return "jdbc:oracle:thin:@" + config.host + ":" + config.port + ":" + config.sid;
        } else {
            return "jdbc:mysql://" + config.host + ":" + config.port + "/" + config.database;
        }
    }

    public static String getDatabaseType() {
        return currentType.name();
    }
}
