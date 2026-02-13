package com.example.roadmap;

import java.sql.*;
import java.util.Properties;

public class MySQLConnection {
    private static Connection connection;
    private static String host = "localhost";
    private static String port = "3306";
    private static String database = "roadmap_mvp";
    private static String username = "root";
    private static String password = "";

    public static void setConnectionParams(String h, String p, String db, String user, String pass) {
        host = h;
        port = p;
        database = db;
        username = user;
        password = pass;
    }

    public static Connection connect() throws Exception {
        // Check if MySQL driver is available
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            // Driver not found, will handle gracefully
            System.out.println("MySQL JDBC driver not found. Using JSON storage only.");
            return null;
        }

        try {
            String url = "jdbc:mysql://" + host + ":" + port + "/" + database + 
                        "?useSSL=false&serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8";
            
            Properties props = new Properties();
            props.setProperty("user", username);
            props.setProperty("password", password);
            props.setProperty("useUnicode", "true");
            props.setProperty("characterEncoding", "UTF-8");

            connection = DriverManager.getConnection(url, props);
            System.out.println("Connected to MySQL database!");
            return connection;
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
            return null;
        }
    }

    public static Connection getConnection() {
        return connection;
    }

    public static void disconnect() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                System.out.println("Disconnected from MySQL database.");
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
        return "jdbc:mysql://" + host + ":" + port + "/" + database;
    }
}
