package com.retailiq.dao;

import com.retailiq.model.InventoryLog;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * InventoryLogDAO - Data Access Object managing inventory stock movements and restocking.
 */
public class InventoryLogDAO {

    private static final Logger LOGGER = Logger.getLogger(InventoryLogDAO.class.getName());

    /**
     * Restocks a product: increases current_stock and inserts a 'PURCHASE_RESTOCK' audit log.
     * Uses an atomic transaction.
     * 
     * @return true if restock succeeded; false otherwise.
     */
    public boolean restockProduct(int productId, int userId, int quantityAdded, String reason) {
        if (quantityAdded <= 0) {
            throw new IllegalArgumentException("Quantity added must be strictly positive.");
        }

        Connection conn = null;
        PreparedStatement pstmtUpdate = null;
        PreparedStatement pstmtLog = null;

        String updateSql = "UPDATE products SET current_stock = current_stock + ? WHERE product_id = ?";
        String logSql = "INSERT INTO inventory_logs (product_id, user_id, change_type, quantity_changed, stock_after_change, reason) " +
                        "VALUES (?, ?, 'PURCHASE_RESTOCK', ?, (SELECT current_stock FROM products WHERE product_id = ?), ?)";

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            pstmtUpdate = conn.prepareStatement(updateSql);
            pstmtUpdate.setInt(1, quantityAdded);
            pstmtUpdate.setInt(2, productId);
            pstmtUpdate.executeUpdate();

            pstmtLog = conn.prepareStatement(logSql);
            pstmtLog.setInt(1, productId);
            pstmtLog.setInt(2, userId);
            pstmtLog.setInt(3, quantityAdded);
            pstmtLog.setInt(4, productId);
            pstmtLog.setString(5, reason != null ? reason : "Supplier Delivery Restock");
            pstmtLog.executeUpdate();

            conn.commit();
            LOGGER.info("Product " + productId + " restocked with " + quantityAdded + " units.");
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {}
            }
            LOGGER.log(Level.SEVERE, "Error restocking product ID: " + productId, e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ignored) {}
            }
            DBConnection.close(null, pstmtUpdate);
            DBConnection.close(conn, pstmtLog);
        }
        return false;
    }

    /**
     * Retrieves the most recent audit logs.
     */
    public List<InventoryLog> findRecent(int limit) {
        List<InventoryLog> list = new ArrayList<>();
        String sql = "SELECT l.log_id, l.product_id, p.sku, p.product_name, l.user_id, u.username, " +
                     "       l.change_type, l.quantity_changed, l.stock_after_change, l.reason, l.log_timestamp " +
                     "FROM inventory_logs l " +
                     "JOIN products p ON l.product_id = p.product_id " +
                     "JOIN users u ON l.user_id = u.user_id " +
                     "ORDER BY l.log_timestamp DESC LIMIT ?";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, limit);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                InventoryLog log = new InventoryLog();
                log.setLogId(rs.getInt("log_id"));
                log.setProductId(rs.getInt("product_id"));
                log.setSku(rs.getString("sku"));
                log.setProductName(rs.getString("product_name"));
                log.setUserId(rs.getInt("user_id"));
                log.setUsername(rs.getString("username"));
                log.setChangeType(rs.getString("change_type"));
                log.setQuantityChanged(rs.getInt("quantity_changed"));
                log.setStockAfterChange(rs.getInt("stock_after_change"));
                log.setReason(rs.getString("reason"));
                log.setLogTimestamp(rs.getTimestamp("log_timestamp"));
                list.add(log);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error querying inventory logs", e);
        } finally {
            DBConnection.close(conn, pstmt, rs);
        }
        return list;
    }
}
