package com.retailiq.dao;

import com.retailiq.exception.InsufficientStockException;
import com.retailiq.exception.ProductNotFoundException;
import com.retailiq.model.SaleItem;
import com.retailiq.model.SaleTransaction;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * SalesDAO - Data Access Object managing sales transactions with ACID Atomicity.
 * 
 * Core Academic Concepts Demonstrated:
 * - ACID Transactions: All-or-nothing atomicity using setAutoCommit(false), commit(), and rollback().
 * - Concurrency & Stock Integrity: Validating existing stock levels before deducting inventory.
 * - Audit Trail Synchronization: Logging every sale deduction into inventory_logs automatically.
 */
public class SalesDAO {

    private static final Logger LOGGER = Logger.getLogger(SalesDAO.class.getName());

    /**
     * Executes a complete multi-item point-of-sale transaction atomically.
     * 
     * @param transaction The header information (userId, paymentMethod, totalAmount)
     * @param items The line items purchased
     * @return Generated invoice number if successful; null on failure or insufficient stock.
     * @throws Exception If transaction fails.
     */
    public String processSale(SaleTransaction transaction, List<SaleItem> items) throws Exception {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Cart cannot be empty for checkout.");
        }

        Connection conn = null;
        PreparedStatement pstmtCheckStock = null;
        PreparedStatement pstmtTx = null;
        PreparedStatement pstmtItem = null;
        PreparedStatement pstmtUpdateStock = null;
        PreparedStatement pstmtLog = null;
        ResultSet rsTx = null;

        String invoiceNumber = generateInvoiceNumber();

        try {
            conn = DBConnection.getConnection();
            // 1. Begin ACID Transaction (Turn off auto-commit)
            conn.setAutoCommit(false);

            // 2. Validate sufficient stock for every line item
            String checkStockSql = "SELECT product_name, current_stock FROM products WHERE product_id = ? FOR UPDATE";
            pstmtCheckStock = conn.prepareStatement(checkStockSql);

            BigDecimal calculatedTotal = BigDecimal.ZERO;
            for (SaleItem item : items) {
                pstmtCheckStock.setInt(1, item.getProductId());
                try (ResultSet rsStock = pstmtCheckStock.executeQuery()) {
                    if (!rsStock.next()) {
                        throw new ProductNotFoundException(item.getProductId());
                    }
                    int availableStock = rsStock.getInt("current_stock");
                    String pName = rsStock.getString("product_name");
                    if (availableStock < item.getQuantity()) {
                        throw new InsufficientStockException(item.getProductId(), pName, item.getQuantity(), availableStock);
                    }
                }
                calculatedTotal = calculatedTotal.add(item.getSubtotal());
            }

            // 3. Insert Sales Transaction Header
            String insertTxSql = "INSERT INTO sales_transactions (user_id, invoice_number, total_amount, payment_method) " +
                                 "VALUES (?, ?, ?, ?)";
            pstmtTx = conn.prepareStatement(insertTxSql, Statement.RETURN_GENERATED_KEYS);
            pstmtTx.setInt(1, transaction.getUserId());
            pstmtTx.setString(2, invoiceNumber);
            pstmtTx.setBigDecimal(3, calculatedTotal);
            pstmtTx.setString(4, transaction.getPaymentMethod());
            pstmtTx.executeUpdate();

            rsTx = pstmtTx.getGeneratedKeys();
            int transactionId;
            if (rsTx.next()) {
                transactionId = rsTx.getInt(1);
            } else {
                throw new SQLException("Failed to retrieve generated transaction ID.");
            }

            // 4. Prepare batch statements for line items, stock updates, and audit logs
            String insertItemSql = "INSERT INTO sale_items (transaction_id, product_id, quantity, unit_price, subtotal) " +
                                   "VALUES (?, ?, ?, ?, ?)";
            String updateStockSql = "UPDATE products SET current_stock = current_stock - ? WHERE product_id = ?";
            String insertLogSql = "INSERT INTO inventory_logs (product_id, user_id, change_type, quantity_changed, stock_after_change, reason) " +
                                  "VALUES (?, ?, 'SALE_DEDUCTION', ?, (SELECT current_stock FROM products WHERE product_id = ?), ?)";

            pstmtItem = conn.prepareStatement(insertItemSql);
            pstmtUpdateStock = conn.prepareStatement(updateStockSql);
            pstmtLog = conn.prepareStatement(insertLogSql);

            for (SaleItem item : items) {
                // A. Insert Line Item
                pstmtItem.setInt(1, transactionId);
                pstmtItem.setInt(2, item.getProductId());
                pstmtItem.setInt(3, item.getQuantity());
                pstmtItem.setBigDecimal(4, item.getUnitPrice());
                pstmtItem.setBigDecimal(5, item.getSubtotal());
                pstmtItem.executeUpdate();

                // B. Deduct Inventory Stock
                pstmtUpdateStock.setInt(1, item.getQuantity());
                pstmtUpdateStock.setInt(2, item.getProductId());
                pstmtUpdateStock.executeUpdate();

                // C. Log Inventory Audit Trail
                pstmtLog.setInt(1, item.getProductId());
                pstmtLog.setInt(2, transaction.getUserId());
                pstmtLog.setInt(3, -item.getQuantity());
                pstmtLog.setInt(4, item.getProductId());
                pstmtLog.setString(5, "Sale Transaction #" + invoiceNumber);
                pstmtLog.executeUpdate();
            }

            // 5. Commit Transaction (All operations succeeded)
            conn.commit();
            LOGGER.info("Successfully committed sale transaction: " + invoiceNumber + " for $" + calculatedTotal);
            return invoiceNumber;

        } catch (Exception e) {
            // 6. Rollback Transaction (Undo all changes if any error occurred)
            if (conn != null) {
                try {
                    conn.rollback();
                    LOGGER.warning("Rolled back sale transaction due to error: " + e.getMessage());
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Failed to rollback transaction", ex);
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Reset to default
                } catch (SQLException ignored) {}
            }
            DBConnection.close(conn, pstmtTx, rsTx);
            DBConnection.close(null, pstmtCheckStock, null);
            DBConnection.close(null, pstmtItem, null);
            DBConnection.close(null, pstmtUpdateStock, null);
            DBConnection.close(null, pstmtLog, null);
        }
    }

    /**
     * Retrieves the most recent sales transactions.
     */
    public List<SaleTransaction> findRecent(int limit) {
        List<SaleTransaction> list = new ArrayList<>();
        String sql = "SELECT s.transaction_id, s.user_id, u.full_name AS cashier_name, s.invoice_number, " +
                     "       s.transaction_date, s.total_amount, s.payment_method " +
                     "FROM sales_transactions s " +
                     "JOIN users u ON s.user_id = u.user_id " +
                     "ORDER BY s.transaction_date DESC LIMIT ?";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, limit);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                SaleTransaction tx = new SaleTransaction();
                tx.setTransactionId(rs.getInt("transaction_id"));
                tx.setUserId(rs.getInt("user_id"));
                tx.setCashierName(rs.getString("cashier_name"));
                tx.setInvoiceNumber(rs.getString("invoice_number"));
                tx.setTransactionDate(rs.getTimestamp("transaction_date"));
                tx.setTotalAmount(rs.getBigDecimal("total_amount"));
                tx.setPaymentMethod(rs.getString("payment_method"));
                list.add(tx);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving recent sales", e);
        } finally {
            DBConnection.close(conn, pstmt, rs);
        }
        return list;
    }

    /**
     * Retrieves a complete transaction with all line items by invoice number.
     */
    public SaleTransaction findByInvoice(String invoiceNumber) {
        String txSql = "SELECT s.transaction_id, s.user_id, u.full_name AS cashier_name, s.invoice_number, " +
                       "       s.transaction_date, s.total_amount, s.payment_method " +
                       "FROM sales_transactions s " +
                       "JOIN users u ON s.user_id = u.user_id " +
                       "WHERE s.invoice_number = ?";

        String itemsSql = "SELECT si.item_id, si.product_id, p.sku, p.product_name, si.quantity, si.unit_price, si.subtotal " +
                          "FROM sale_items si " +
                          "JOIN products p ON si.product_id = p.product_id " +
                          "WHERE si.transaction_id = ?";

        Connection conn = null;
        PreparedStatement pstmtTx = null;
        PreparedStatement pstmtItems = null;
        ResultSet rsTx = null;
        ResultSet rsItems = null;

        try {
            conn = DBConnection.getConnection();
            pstmtTx = conn.prepareStatement(txSql);
            pstmtTx.setString(1, invoiceNumber);
            rsTx = pstmtTx.executeQuery();

            if (rsTx.next()) {
                SaleTransaction tx = new SaleTransaction();
                tx.setTransactionId(rsTx.getInt("transaction_id"));
                tx.setUserId(rsTx.getInt("user_id"));
                tx.setCashierName(rsTx.getString("cashier_name"));
                tx.setInvoiceNumber(rsTx.getString("invoice_number"));
                tx.setTransactionDate(rsTx.getTimestamp("transaction_date"));
                tx.setTotalAmount(rsTx.getBigDecimal("total_amount"));
                tx.setPaymentMethod(rsTx.getString("payment_method"));

                // Fetch line items
                pstmtItems = conn.prepareStatement(itemsSql);
                pstmtItems.setInt(1, tx.getTransactionId());
                rsItems = pstmtItems.executeQuery();

                while (rsItems.next()) {
                    SaleItem item = new SaleItem();
                    item.setItemId(rsItems.getInt("item_id"));
                    item.setTransactionId(tx.getTransactionId());
                    item.setProductId(rsItems.getInt("product_id"));
                    item.setSku(rsItems.getString("sku"));
                    item.setProductName(rsItems.getString("product_name"));
                    item.setQuantity(rsItems.getInt("quantity"));
                    item.setUnitPrice(rsItems.getBigDecimal("unit_price"));
                    item.setSubtotal(rsItems.getBigDecimal("subtotal"));
                    tx.addItem(item);
                }
                return tx;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching transaction by invoice: " + invoiceNumber, e);
        } finally {
            DBConnection.close(null, pstmtItems, rsItems);
            DBConnection.close(conn, pstmtTx, rsTx);
        }
        return null;
    }

    private String generateInvoiceNumber() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
        int randomSuffix = 100 + (int)(Math.random() * 900);
        return "INV-" + sdf.format(new Date()) + "-" + randomSuffix;
    }
}
