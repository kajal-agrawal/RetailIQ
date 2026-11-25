package com.retailiq.dao;

import com.retailiq.model.Product;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ProductDAO - Data Access Object for complete Product Catalog CRUD operations.
 * 
 * Provides:
 * - Create: insert(product)
 * - Read: findAll(), findById(id), findLowStock()
 * - Update: update(product)
 * - Delete: delete(id)
 */
public class ProductDAO implements GenericDAO<Product> {

    private static final Logger LOGGER = Logger.getLogger(ProductDAO.class.getName());

    /**
     * Retrieves all products with category names.
     */
    public List<Product> findAll() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.product_id, p.category_id, c.category_name, p.sku, p.product_name, " +
                     "       p.cost_price, p.unit_price, p.current_stock, p.safety_stock, " +
                     "       p.lead_time_days, p.created_at " +
                     "FROM products p " +
                     "JOIN categories c ON p.category_id = c.category_id " +
                     "ORDER BY p.product_name ASC";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving products", e);
        } finally {
            DBConnection.close(conn, pstmt, rs);
        }
        return products;
    }

    /**
     * Retrieves a single product by primary key.
     */
    public Product findById(int productId) {
        String sql = "SELECT p.product_id, p.category_id, c.category_name, p.sku, p.product_name, " +
                     "       p.cost_price, p.unit_price, p.current_stock, p.safety_stock, " +
                     "       p.lead_time_days, p.created_at " +
                     "FROM products p " +
                     "JOIN categories c ON p.category_id = c.category_id " +
                     "WHERE p.product_id = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, productId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToProduct(rs);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving product by ID: " + productId, e);
        } finally {
            DBConnection.close(conn, pstmt, rs);
        }
        return null;
    }

    /**
     * Inserts a new product into the database.
     * 
     * @return generated product_id, or -1 on failure.
     */
    public int insert(Product p) {
        String sql = "INSERT INTO products (category_id, sku, product_name, cost_price, unit_price, " +
                     "                     current_stock, safety_stock, lead_time_days) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, p.getCategoryId());
            pstmt.setString(2, p.getSku());
            pstmt.setString(3, p.getProductName());
            pstmt.setBigDecimal(4, p.getCostPrice());
            pstmt.setBigDecimal(5, p.getUnitPrice());
            pstmt.setInt(6, p.getCurrentStock());
            pstmt.setInt(7, p.getSafetyStock());
            pstmt.setInt(8, p.getLeadTimeDays());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error inserting new product: " + p.getProductName(), e);
        } finally {
            DBConnection.close(conn, pstmt, rs);
        }
        return -1;
    }

    /**
     * Updates an existing product in the database.
     */
    public boolean update(Product p) {
        String sql = "UPDATE products SET category_id = ?, sku = ?, product_name = ?, cost_price = ?, " +
                     "                   unit_price = ?, current_stock = ?, safety_stock = ?, lead_time_days = ? " +
                     "WHERE product_id = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, p.getCategoryId());
            pstmt.setString(2, p.getSku());
            pstmt.setString(3, p.getProductName());
            pstmt.setBigDecimal(4, p.getCostPrice());
            pstmt.setBigDecimal(5, p.getUnitPrice());
            pstmt.setInt(6, p.getCurrentStock());
            pstmt.setInt(7, p.getSafetyStock());
            pstmt.setInt(8, p.getLeadTimeDays());
            pstmt.setInt(9, p.getProductId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating product ID: " + p.getProductId(), e);
        } finally {
            DBConnection.close(conn, pstmt);
        }
        return false;
    }

    /**
     * Deletes a product by ID.
     */
    public boolean delete(int productId) {
        String sql = "DELETE FROM products WHERE product_id = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, productId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting product ID: " + productId, e);
        } finally {
            DBConnection.close(conn, pstmt);
        }
        return false;
    }

    /**
     * Retrieves products where currentStock <= safetyStock (Inventory Alert Trigger).
     */
    public List<Product> findLowStock() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.product_id, p.category_id, c.category_name, p.sku, p.product_name, " +
                     "       p.cost_price, p.unit_price, p.current_stock, p.safety_stock, " +
                     "       p.lead_time_days, p.created_at " +
                     "FROM products p " +
                     "JOIN categories c ON p.category_id = c.category_id " +
                     "WHERE p.current_stock <= p.safety_stock " +
                     "ORDER BY p.current_stock ASC";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error querying low stock products", e);
        } finally {
            DBConnection.close(conn, pstmt, rs);
        }
        return products;
    }

    // Helper mapper from ResultSet to Product entity
    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setProductId(rs.getInt("product_id"));
        p.setCategoryId(rs.getInt("category_id"));
        p.setCategoryName(rs.getString("category_name"));
        p.setSku(rs.getString("sku"));
        p.setProductName(rs.getString("product_name"));
        p.setCostPrice(rs.getBigDecimal("cost_price"));
        p.setUnitPrice(rs.getBigDecimal("unit_price"));
        p.setCurrentStock(rs.getInt("current_stock"));
        p.setSafetyStock(rs.getInt("safety_stock"));
        p.setLeadTimeDays(rs.getInt("lead_time_days"));
        p.setCreatedAt(rs.getTimestamp("created_at"));
        return p;
    }
}
