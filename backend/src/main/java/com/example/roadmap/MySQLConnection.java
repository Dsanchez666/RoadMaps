package com.example.roadmap;

import java.sql.Connection;

/**
 * Compatibility facade for legacy code that expects a MySQL-specific helper.
 *
 * Internally this class delegates to {@link DatabaseConnection} and keeps
 * backward compatibility while the system supports multiple engines.
 *
 * @since 1.0
 */
public class MySQLConnection {

    /**
     * Sets MySQL connection parameters and marks MySQL as active engine.
     *
     * @param host MySQL host.
     * @param port MySQL port.
     * @param database MySQL database name.
     * @param user MySQL username.
     * @param password MySQL password.
     */
    public static void setConnectionParams(String host, String port, String database, String user, String password) {
        DatabaseConnection.setMySQLParams(host, port, database, user, password);
        DatabaseConnection.setDatabaseType("MYSQL");
    }

    /**
     * Connects to MySQL using current configuration.
     *
     * @return JDBC connection or null.
     * @throws Exception Connection/driver errors.
     */
    public static Connection connect() throws Exception {
        DatabaseConnection.setDatabaseType("MYSQL");
        return DatabaseConnection.connect();
    }

    /**
     * Connects to Oracle using SID parameters.
     *
     * @param host Oracle host.
     * @param port Oracle port.
     * @param sid Oracle SID.
     * @param user Oracle username.
     * @param password Oracle password.
     * @return JDBC connection or null.
     * @throws Exception Connection/driver errors.
     */
    public static Connection connectToOracle(String host, String port, String sid, String user, String password) throws Exception {
        DatabaseConnection.setOracleParams(host, port, sid, user, password);
        DatabaseConnection.setDatabaseType("ORACLE");
        return DatabaseConnection.connect();
    }

    /** @return Active JDBC connection. */
    public static Connection getConnection() {
        return DatabaseConnection.getConnection();
    }

    /** Closes current database connection. */
    public static void disconnect() {
        DatabaseConnection.disconnect();
    }

    /** @return true when the connection is active. */
    public static boolean isConnected() {
        return DatabaseConnection.isConnected();
    }

    /** @return Current JDBC URL. */
    public static String getConnectionInfo() {
        return DatabaseConnection.getConnectionInfo();
    }
}