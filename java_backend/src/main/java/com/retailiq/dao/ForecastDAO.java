package com.retailiq.dao;

import com.retailiq.model.HistoricalSalePoint;
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
 * ForecastDAO - Data Access Object for sales time-series queries and forecast log persistence.
 */
public class ForecastDAO {

    private static final Logger LOGGER = Logger.getLogger(ForecastDAO.class.getName());

    /**
     * Retrieves aggregated daily sales quantities for a product.
     * Ordered chronologically for ML training and statistical analysis.
     */
    public List<HistoricalSalePoint> getHistoricalDailySales(int productId) {
        List<HistoricalSalePoint> list = new ArrayList<>();
        String sql = "SELECT DATE(st.transaction_date) AS sale_date, SUM(si.quantity) AS total_qty " +
                     "FROM sale_items si " +
                     "JOIN sales_transactions st ON si.transaction_id = st.transaction_id " +
                     "WHERE si.product_id = ? " +
                     "GROUP BY DATE(st.transaction_date) " +
                     "ORDER BY sale_date ASC";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, productId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                list.add(new HistoricalSalePoint(rs.getString("sale_date"), rs.getInt("total_qty")));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching historical daily sales for product " + productId, e);
        } finally {
            DBConnection.close(conn, pstmt, rs);
        }
        return list;
    }

    /**
     * Persists an AI forecast and recommendation record into forecast_logs.
     */
    public int saveForecastLog(int productId, String modelUsed, int horizonDays, 
                              double predictedSales, double safetyStock, 
                              int recommendedQty, String alertStatus) {
        String sql = "INSERT INTO forecast_logs (product_id, model_used, forecast_horizon_days, " +
                     "                         predicted_sales, calculated_safety_stock, " +
                     "                         recommended_reorder_qty, alert_status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, productId);
            pstmt.setString(2, modelUsed);
            pstmt.setInt(3, horizonDays);
            pstmt.setDouble(4, predictedSales);
            pstmt.setDouble(5, safetyStock);
            pstmt.setInt(6, recommendedQty);
            pstmt.setString(7, alertStatus);

            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving forecast log", e);
        } finally {
            DBConnection.close(conn, pstmt, rs);
        }
        return -1;
    }
}
