package com.retailiq.dao;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * DBConnectionTest - Standalone runner to test and verify MySQL connection.
 * 
 * Can be run from command line or IDE to verify connectivity before running the web app.
 */
public class DBConnectionTest {

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("   RetailIQ: Testing Database Connection");
        System.out.println("=================================================");

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            if (conn != null && !conn.isClosed()) {
                System.out.println("[SUCCESS] Connected to MySQL database successfully!");
                
                // Print database metadata
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("-> Database Product: " + meta.getDatabaseProductName());
                System.out.println("-> Product Version : " + meta.getDatabaseProductVersion());
                System.out.println("-> Driver Name     : " + meta.getDriverName());
                System.out.println("-------------------------------------------------");

                // Execute sample verification query
                stmt = conn.createStatement();
                rs = stmt.executeQuery("SELECT COUNT(*) AS total_products FROM products");
                if (rs.next()) {
                    int count = rs.getInt("total_products");
                    System.out.println("[QUERY VERIFICATION] Found " + count + " products in 'products' table.");
                }
                System.out.println("=================================================");
                System.out.println("   Database setup and JDBC layer verified!");
                System.out.println("=================================================");
            }
        } catch (Exception e) {
            System.err.println("[FAILURE] Could not connect to database!");
            System.err.println("Error details: " + e.getMessage());
            System.err.println("\nPlease check:");
            System.err.println("1. Is MySQL server running?");
            System.err.println("2. Does 'retail_iq_db' exist? (Run database/schema.sql)");
            System.err.println("3. Check user and password inside src/main/resources/db.properties");
        } finally {
            DBConnection.close(conn, stmt, rs);
        }
    }
}
