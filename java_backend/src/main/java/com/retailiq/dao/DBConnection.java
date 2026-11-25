package com.retailiq.dao;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DBConnection - Database Connection Management Class.
 * 
 * Implements the Singleton / Centralized Factory design pattern to manage
 * JDBC connections cleanly, reading credentials from 'db.properties'.
 * 
 * Academic Concepts Demonstrated:
 * - Resource encapsulation (Object-Oriented Programming).
 * - Safe exception handling and resource deallocation.
 * - Centralized configuration decoupling (avoiding hardcoded credentials).
 */
public class DBConnection {

    private static final Logger LOGGER = Logger.getLogger(DBConnection.class.getName());
    private static final Properties props = new Properties();

    // Default connection parameters (fallbacks if db.properties is missing)
    private static String dbUrl = "jdbc:mysql://localhost:3306/retail_iq_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static String dbUser = "root";
    private static String dbPassword = "root";
    private static String dbDriver = "com.mysql.cj.jdbc.Driver";

    static {
        // Load properties file from the classpath once during class loading
        try (InputStream in = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) {
                props.load(in);
                dbUrl = props.getProperty("db.url", dbUrl);
                dbUser = props.getProperty("db.user", dbUser);
                dbPassword = props.getProperty("db.password", dbPassword);
                dbDriver = props.getProperty("db.driver", dbDriver);
                LOGGER.info("Successfully loaded db.properties configuration.");
            } else {
                LOGGER.warning("db.properties not found in classpath. Using default credentials.");
            }
            // Register MySQL JDBC Driver
            Class.forName(dbDriver);
            LOGGER.info("MySQL JDBC Driver registered successfully: " + dbDriver);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error initializing DBConnection driver/properties: " + e.getMessage(), e);
        }
    }

    // Private constructor to prevent direct instantiation (Singleton Principle)
    private DBConnection() {}

    /**
     * Establishes and returns an active JDBC Connection.
     * 
     * @return Connection to MySQL database.
     * @throws SQLException If a database access error occurs.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }

    /**
     * Helper method to safely close JDBC resources (Connection, Statement, ResultSet).
     * Prevents resource leakage in the application server.
     */
    public static void close(Connection conn, Statement stmt, ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                LOGGER.warning("Failed to close ResultSet: " + e.getMessage());
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                LOGGER.warning("Failed to close Statement: " + e.getMessage());
            }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                LOGGER.warning("Failed to close Connection: " + e.getMessage());
            }
        }
    }

    /**
     * Overloaded helper for queries that do not have a ResultSet (e.g. INSERT, UPDATE).
     */
    public static void close(Connection conn, Statement stmt) {
        close(conn, stmt, null);
    }
}
