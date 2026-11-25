package com.retailiq.dao;

import com.retailiq.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * UserDAO - Data Access Object for User authentication and profile retrieval.
 * 
 * Academic Principle:
 * - Data Access Object (DAO) Pattern: Separates business logic from database operations.
 * - Defensive coding: Uses PreparedStatements with parameterized queries to prevent SQL Injection.
 */
public class UserDAO {

    private static final Logger LOGGER = Logger.getLogger(UserDAO.class.getName());

    /**
     * Authenticates a user by username and password.
     * 
     * @param username The login username
     * @param password The plaintext or hashed password
     * @return User object if credentials are valid; null otherwise.
     */
    public User authenticate(String username, String password) {
        String sql = "SELECT user_id, username, password_hash, full_name, role, created_at " +
                     "FROM users WHERE username = ? AND password_hash = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, password);

            rs = pstmt.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setPasswordHash(rs.getString("password_hash"));
                user.setFullName(rs.getString("full_name"));
                user.setRole(rs.getString("role"));
                user.setCreatedAt(rs.getTimestamp("created_at"));
                return user;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Authentication error for user: " + username, e);
        } finally {
            DBConnection.close(conn, pstmt, rs);
        }
        return null;
    }

    /**
     * Finds a user by their unique user_id.
     */
    public User findById(int userId) {
        String sql = "SELECT user_id, username, password_hash, full_name, role, created_at " +
                     "FROM users WHERE user_id = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);

            rs = pstmt.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setPasswordHash(rs.getString("password_hash"));
                user.setFullName(rs.getString("full_name"));
                user.setRole(rs.getString("role"));
                user.setCreatedAt(rs.getTimestamp("created_at"));
                return user;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding user by ID: " + userId, e);
        } finally {
            DBConnection.close(conn, pstmt, rs);
        }
        return null;
    }
}
