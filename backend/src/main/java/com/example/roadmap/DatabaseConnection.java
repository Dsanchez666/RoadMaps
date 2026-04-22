package com.example.roadmap;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
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

    private static HikariDataSource dataSource;
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

        // Close existing pool before recreating it.
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            dataSource = null;
        }

        if (currentType == DatabaseType.ORACLE) {
            initializeOracleDataSource(config);
        } else {
            initializeMySqlDataSource(config);
        }

        if (dataSource == null) {
            return null;
        }

        try {
            Connection conn = dataSource.getConnection();
            System.out.println("Connected to " + currentType + ": " + config);
            return conn;
        } catch (SQLException e) {
            System.err.println("Connection failed: " + e.getMessage());
            return null;
        }
    }

    private static void initializeMySqlDataSource(Config config) throws Exception {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("MySQL JDBC driver not found. Using JSON storage only.");
            return;
        }

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:mysql://" + config.host + ":" + config.port + "/" + config.database
            + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8");
        hikariConfig.setUsername(config.user);
        hikariConfig.setPassword(config.password);
        hikariConfig.setMaximumPoolSize(10);
        hikariConfig.setMinimumIdle(2);
        hikariConfig.setPoolName("roadmap-mysql-pool");
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        dataSource = new HikariDataSource(hikariConfig);
    }

    private static void initializeOracleDataSource(Config config) throws Exception {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException e) {
            System.out.println("Oracle JDBC driver not found. Using JSON storage only.");
            return;
        }

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:oracle:thin:@" + config.host + ":" + config.port + ":" + config.sid);
        hikariConfig.setUsername(config.user);
        hikariConfig.setPassword(config.password);
        hikariConfig.setMaximumPoolSize(10);
        hikariConfig.setMinimumIdle(2);
        hikariConfig.setPoolName("roadmap-oracle-pool");
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        dataSource = new HikariDataSource(hikariConfig);
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
     * Returns a pooled JDBC connection from the active data source.
     *
     * @return Connection instance or null when pool is not configured.
     */
    public static Connection getConnection() {
        if (dataSource == null || dataSource.isClosed()) {
            return null;
        }
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            System.err.println("Error obtaining pooled connection: " + e.getMessage());
            return null;
        }
    }

    /**
     * Closes the current connection pool.
     */
    public static void disconnect() {
        if (dataSource != null) {
            try {
                dataSource.close();
            } catch (Exception e) {
                System.err.println("Error closing connection pool: " + e.getMessage());
            } finally {
                dataSource = null;
            }
            System.out.println("Disconnected from database.");
        }
    }

    /**
     * Checks whether the current data source is available.
     *
     * @return true when the pool exists and is not closed.
     */
    public static boolean isConnected() {
        return dataSource != null && !dataSource.isClosed();
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
