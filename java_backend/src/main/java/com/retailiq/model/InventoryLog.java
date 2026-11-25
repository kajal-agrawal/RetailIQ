package com.retailiq.model;

import java.sql.Timestamp;

/**
 * InventoryLog - Entity Model representing an audit trail record of stock movements.
 * 
 * Supports change types:
 * - 'PURCHASE_RESTOCK'
 * - 'SALE_DEDUCTION'
 * - 'MANUAL_ADJUSTMENT'
 * - 'DAMAGE'
 */
public class InventoryLog implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    private int logId;
    private int productId;
    private String sku;          // Display helper
    private String productName;  // Display helper
    private int userId;
    private String username;     // Display helper
    private String changeType;
    private int quantityChanged;
    private int stockAfterChange;
    private String reason;
    private Timestamp logTimestamp;

    public InventoryLog() {}

    public InventoryLog(int productId, int userId, String changeType, 
                        int quantityChanged, int stockAfterChange, String reason) {
        this.productId = productId;
        this.userId = userId;
        this.changeType = changeType;
        this.quantityChanged = quantityChanged;
        this.stockAfterChange = stockAfterChange;
        this.reason = reason;
    }

    public int getLogId() {
        return logId;
    }

    public void setLogId(int logId) {
        this.logId = logId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getChangeType() {
        return changeType;
    }

    public void setChangeType(String changeType) {
        this.changeType = changeType;
    }

    public int getQuantityChanged() {
        return quantityChanged;
    }

    public void setQuantityChanged(int quantityChanged) {
        this.quantityChanged = quantityChanged;
    }

    public int getStockAfterChange() {
        return stockAfterChange;
    }

    public void setStockAfterChange(int stockAfterChange) {
        this.stockAfterChange = stockAfterChange;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Timestamp getLogTimestamp() {
        return logTimestamp;
    }

    public void setLogTimestamp(Timestamp logTimestamp) {
        this.logTimestamp = logTimestamp;
    }
}
