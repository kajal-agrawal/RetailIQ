package com.retailiq.dao;

import com.retailiq.model.CategoryRevenue;
import com.retailiq.model.ForecastLogEntry;
import com.retailiq.model.TopProductMetric;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * AnalyticsDAO - Business Intelligence & Retail Analytics Data Access Object.
 * 
 * Academic Discipline:
 * - Data Analytics: KPI aggregations, revenue trends, Pareto velocity,
 *   and Inventory Turnover Ratio (ITR).
 */
public class AnalyticsDAO {

    private static final Logger LOGGER = Logger.getLogger(AnalyticsDAO.class.getName());

    public BigDecimal getTotalRevenue() {
        String sql = "SELECT COALESCE(SUM(total_amount), 0.00) AS total_rev FROM sales_transactions";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rs.getBigDecimal("total_rev");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching total revenue", e);
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal getMonthlyRevenue() {
        String sql = "SELECT COALESCE(SUM(total_amount), 0.00) AS month_rev FROM sales_transactions " +
                     "WHERE MONTH(transaction_date) = MONTH(CURRENT_DATE()) " +
                     "  AND YEAR(transaction_date) = YEAR(CURRENT_DATE())";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rs.getBigDecimal("month_rev");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching monthly revenue", e);
        }
        return BigDecimal.ZERO;
    }

    public int getTotalOrdersCount() {
        String sql = "SELECT COUNT(*) AS total_orders FROM sales_transactions";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("total_orders");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error counting total orders", e);
        }
        return 0;
    }

    public int getLowStockAlertCount() {
        String sql = "SELECT COUNT(*) AS low_stock_count FROM products WHERE current_stock <= safety_stock";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("low_stock_count");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error counting low stock products", e);
        }
        return 0;
    }

    public int getTotalCatalogCount() {
        String sql = "SELECT COUNT(*) AS prod_count FROM products";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("prod_count");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error counting products", e);
        }
        return 0;
    }

    /**
     * Calculates the Inventory Turnover Ratio (ITR).
     * 
     * Formula:
     *   ITR = Cost of Goods Sold (COGS) / Current Total Inventory Cost Value
     */
    public double getInventoryTurnoverRatio() {
        String cogsSql = "SELECT COALESCE(SUM(si.quantity * p.cost_price), 0.00) AS total_cogs " +
                         "FROM sale_items si JOIN products p ON si.product_id = p.product_id";
        String invValSql = "SELECT COALESCE(SUM(current_stock * cost_price), 0.00) AS total_inv_val FROM products";

        try (Connection conn = DBConnection.getConnection()) {
            double cogs = 0.0;
            double invVal = 0.0;

            try (PreparedStatement pstmt = conn.prepareStatement(cogsSql);
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) cogs = rs.getDouble("total_cogs");
            }

            try (PreparedStatement pstmt = conn.prepareStatement(invValSql);
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) invVal = rs.getDouble("total_inv_val");
            }

            if (invVal > 0) {
                return Math.round((cogs / invVal) * 100.0) / 100.0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error calculating Inventory Turnover Ratio", e);
        }
        return 0.0;
    }

    /**
     * Retrieves category revenue distribution with percentage share.
     */
    public List<CategoryRevenue> getCategoryRevenueBreakdown() {
        List<CategoryRevenue> list = new ArrayList<>();
        BigDecimal total = getTotalRevenue();
        if (total.compareTo(BigDecimal.ZERO) == 0) return list;

        String sql = "SELECT c.category_name, COALESCE(SUM(si.subtotal), 0.00) AS cat_revenue " +
                     "FROM categories c " +
                     "JOIN products p ON c.category_id = p.category_id " +
                     "JOIN sale_items si ON p.product_id = si.product_id " +
                     "GROUP BY c.category_id, c.category_name " +
                     "ORDER BY cat_revenue DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String catName = rs.getString("category_name");
                BigDecimal rev = rs.getBigDecimal("cat_revenue");
                double pct = (rev.doubleValue() / total.doubleValue()) * 100.0;
                list.add(new CategoryRevenue(catName, rev, Math.round(pct * 10.0) / 10.0));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error querying category breakdown", e);
        }
        return list;
    }

    /**
     * Retrieves fast-moving products sorted by sales volume and gross revenue.
     */
    public List<TopProductMetric> getTopSellingProducts(int limit) {
        List<TopProductMetric> list = new ArrayList<>();
        String sql = "SELECT p.product_id, p.sku, p.product_name, c.category_name, " +
                     "       SUM(si.quantity) AS total_units, " +
                     "       SUM(si.subtotal) AS total_rev, " +
                     "       AVG((p.unit_price - p.cost_price) / p.unit_price * 100) AS avg_margin " +
                     "FROM products p " +
                     "JOIN categories c ON p.category_id = c.category_id " +
                     "JOIN sale_items si ON p.product_id = si.product_id " +
                     "GROUP BY p.product_id, p.sku, p.product_name, c.category_name " +
                     "ORDER BY total_units DESC LIMIT ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    TopProductMetric m = new TopProductMetric();
                    m.setProductId(rs.getInt("product_id"));
                    m.setSku(rs.getString("sku"));
                    m.setProductName(rs.getString("product_name"));
                    m.setCategoryName(rs.getString("category_name"));
                    m.setTotalUnitsSold(rs.getInt("total_units"));
                    m.setTotalRevenue(rs.getBigDecimal("total_rev"));
                    m.setGrossMarginPercentage(Math.round(rs.getDouble("avg_margin") * 10.0) / 10.0);
                    list.add(m);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error querying top selling products", e);
        }
        return list;
    }

    /**
     * Retrieves recent daily sales totals for timeline visualization.
     */
    public Map<String, Double> getRecentDailySalesTimeline(int days) {
        Map<String, Double> timeline = new LinkedHashMap<>();
        String sql = "SELECT DATE(transaction_date) AS t_date, SUM(total_amount) AS daily_rev " +
                     "FROM sales_transactions " +
                     "WHERE transaction_date >= DATE_SUB(CURRENT_DATE(), INTERVAL ? DAY) " +
                     "GROUP BY DATE(transaction_date) " +
                     "ORDER BY t_date ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, days);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    timeline.put(rs.getString("t_date"), rs.getDouble("daily_rev"));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error querying sales timeline", e);
        }
        return timeline;
    }

    /**
     * Retrieves historical AI forecast evaluation logs.
     */
    public List<ForecastLogEntry> getRecentForecastLogs(int limit) {
        List<ForecastLogEntry> list = new ArrayList<>();
        String sql = "SELECT fl.forecast_id, fl.product_id, p.sku, p.product_name, " +
                     "       fl.model_used, fl.forecast_horizon_days, fl.predicted_sales, " +
                     "       fl.calculated_safety_stock, fl.recommended_reorder_qty, " +
                     "       fl.alert_status, fl.generated_at " +
                     "FROM forecast_logs fl " +
                     "JOIN products p ON fl.product_id = p.product_id " +
                     "ORDER BY fl.generated_at DESC LIMIT ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ForecastLogEntry entry = new ForecastLogEntry();
                    entry.setForecastId(rs.getInt("forecast_id"));
                    entry.setProductId(rs.getInt("product_id"));
                    entry.setSku(rs.getString("sku"));
                    entry.setProductName(rs.getString("product_name"));
                    entry.setModelUsed(rs.getString("model_used"));
                    entry.setHorizonDays(rs.getInt("forecast_horizon_days"));
                    entry.setPredictedSales(rs.getDouble("predicted_sales"));
                    entry.setSafetyStock(rs.getDouble("calculated_safety_stock"));
                    entry.setRecommendedReorderQty(rs.getInt("recommended_reorder_qty"));
                    entry.setAlertStatus(rs.getString("alert_status"));
                    entry.setGeneratedAt(rs.getTimestamp("generated_at"));
                    list.add(entry);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error querying forecast logs", e);
        }
        return list;
    }
}
