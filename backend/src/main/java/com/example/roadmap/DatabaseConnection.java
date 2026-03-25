package com.example.roadmap;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Runtime database connection manager with support for MySQL and Oracle.
 *
 * This class centralizes database configuration and keeps a single active
 * connection that can be switched between engines at runtime.
 *
 * @since 1.0
 */
public class DatabaseConnection {

    /** Supported database engines. */
    enum DatabaseType {
        MYSQL, ORACLE
    }

    /**
     * Connection configuration container for one database engine.
     */
    static class Config {
        DatabaseType type;
        String host;
        String port;
        String user;
        String password;
        String database;
        String sid;

        Config(DatabaseType type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return String.format("[%s] host=%s, port=%s, user=%s",
                type, host, port, user);
        }
    }

    private static Connection connection;
    private static DatabaseType currentType = DatabaseType.MYSQL;

    private static final Config mysqlConfig = new Config(DatabaseType.MYSQL);
    private static final Config oracleConfig = new Config(DatabaseType.ORACLE);

    static {
        mysqlConfig.host = "localhost";
        mysqlConfig.port = "3306";
        mysqlConfig.database = "roadmap_mvp";
        mysqlConfig.user = "root";
        mysqlConfig.password = "";

        oracleConfig.host = "localhost";
        oracleConfig.port = "1521";
        oracleConfig.sid = "ORCL";
        oracleConfig.user = "system";
        oracleConfig.password = "";
    }

    /**
     * Updates MySQL connection parameters.
     *
     * @param host MySQL host.
     * @param port MySQL port.
     * @param database MySQL database name.
     * @param user MySQL username.
     * @param password MySQL password.
     */
    public static void setMySQLParams(String host, String port, String database, String user, String password) {
        mysqlConfig.host = host;
        mysqlConfig.port = port;
        mysqlConfig.database = database;
        mysqlConfig.user = user;
        mysqlConfig.password = password;
    }

    /**
     * Updates Oracle connection parameters.
     *
     * @param host Oracle host.
     * @param port Oracle listener port.
     * @param sid Oracle SID.
     * @param user Oracle username.
     * @param password Oracle password.
     */
    public static void setOracleParams(String host, String port, String sid, String user, String password) {
        oracleConfig.host = host;
        oracleConfig.port = port;
        oracleConfig.sid = sid;
        oracleConfig.user = user;
        oracleConfig.password = password;
    }

    /**
     * Sets active database type.
     *
     * @param type Database type value (MYSQL or ORACLE).
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
     * Opens a connection based on the currently selected database type.
     *
     * @return Active JDBC connection, or null when driver/connectivity is unavailable.
     * @throws Exception Unexpected configuration/driver errors.
     */
    public static Connection connect() throws Exception {
        Config config = getCurrentConfig();

        // Routing rule: one entry point delegates to the engine-specific strategy.
        if (currentType == DatabaseType.ORACLE) {
            return connectToOracle(config);
        } else {
            return connectToMySQL(config);
        }
    }

    /**
     * Opens a MySQL JDBC connection.
     *
     * @param config Active MySQL configuration.
     * @return JDBC connection or null when connection cannot be established.
     * @throws Exception Driver lookup or JDBC errors.
     */
    private static Connection connectToMySQL(Config config) throws Exception {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("MySQL JDBC driver not found. Using JSON storage only.");
            return null;
        }

        try {
            String url = "jdbc:mysql://" + config.host + ":" + config.port + "/" + config.database +
                        "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8";

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
     * Opens an Oracle JDBC connection using SID format.
     *
     * @param config Active Oracle configuration.
     * @return JDBC connection or null when connection cannot be established.
     * @throws Exception Driver lookup or JDBC errors.
     */
    private static Connection connectToOracle(Config config) throws Exception {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException e) {
            System.out.println("Oracle JDBC driver not found. Using JSON storage only.");
            return null;
        }

        try {
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

    /**
     * Returns configuration for the currently selected database type.
     *
     * @return Current connection config.
     */
    private static Config getCurrentConfig() {
        return (currentType == DatabaseType.ORACLE) ? oracleConfig : mysqlConfig;
    }

    /**
     * Returns current active JDBC connection reference.
     *
     * @return Connection instance or null.
     */
    public static Connection getConnection() {
        return connection;
    }

    /**
     * Closes active connection if present.
     */
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

    /**
     * Checks if active connection is open.
     *
     * @return true when connection exists and is not closed.
     */
    public static boolean isConnected() {
        if (connection == null) return false;
        try {
            return !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Returns JDBC URL for current configuration.
     *
     * @return Connection URL string.
     */
    public static String getConnectionInfo() {
        Config config = getCurrentConfig();
        if (currentType == DatabaseType.ORACLE) {
            return "jdbc:oracle:thin:@" + config.host + ":" + config.port + ":" + config.sid;
        } else {
            return "jdbc:mysql://" + config.host + ":" + config.port + "/" + config.database;
        }
    }

    /**
     * Returns active database type name.
     *
     * @return MYSQL or ORACLE.
     */
    public static String getDatabaseType() {
        return currentType.name();
    }
}
