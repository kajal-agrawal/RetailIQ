package com.retailiq.dao;

import com.retailiq.model.Category;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * CategoryDAO - Data Access Object for Category lookups.
 */
public class CategoryDAO {

    private static final Logger LOGGER = Logger.getLogger(CategoryDAO.class.getName());

    /**
     * Retrieves all categories for dropdown selection in product forms.
     */
    public List<Category> findAll() {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT category_id, category_name, description, created_at FROM categories ORDER BY category_name ASC";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Category c = new Category();
                c.setCategoryId(rs.getInt("category_id"));
                c.setCategoryName(rs.getString("category_name"));
                c.setDescription(rs.getString("description"));
                c.setCreatedAt(rs.getTimestamp("created_at"));
                list.add(c);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching all categories", e);
        } finally {
            DBConnection.close(conn, pstmt, rs);
        }
        return list;
    }

    /**
     * Retrieves category by primary key.
     */
    public Category findById(int categoryId) {
        String sql = "SELECT category_id, category_name, description, created_at FROM categories WHERE category_id = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, categoryId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                Category c = new Category();
                c.setCategoryId(rs.getInt("category_id"));
                c.setCategoryName(rs.getString("category_name"));
                c.setDescription(rs.getString("description"));
                c.setCreatedAt(rs.getTimestamp("created_at"));
                return c;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching category by ID: " + categoryId, e);
        } finally {
            DBConnection.close(conn, pstmt, rs);
        }
        return null;
    }
}
