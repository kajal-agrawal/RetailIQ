package com.retailiq.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Product - Entity Model representing a retail inventory item.
 * 
 * Key Attributes for Inventory Management:
 * - currentStock: Existing units in warehouse/store.
 * - safetyStock: Buffer stock calculated to prevent stockouts.
 * - leadTimeDays: Time required by vendor to fulfill orders.
 */
public class Product implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    private int productId;
    private int categoryId;
    private String categoryName; // Joined from categories table for UI display
    private String sku;
    private String productName;
    private BigDecimal costPrice;
    private BigDecimal unitPrice;
    private int currentStock;
    private int safetyStock;
    private int leadTimeDays;
    private Timestamp createdAt;

    public Product() {}

    public Product(int productId, int categoryId, String sku, String productName, 
                   BigDecimal costPrice, BigDecimal unitPrice, int currentStock, 
                   int safetyStock, int leadTimeDays) {
        this.productId = productId;
        this.categoryId = categoryId;
        this.sku = sku;
        this.productName = productName;
        this.costPrice = costPrice;
        this.unitPrice = unitPrice;
        this.currentStock = currentStock;
        this.safetyStock = safetyStock;
        this.leadTimeDays = leadTimeDays;
    }

    // Helper method to compute gross margin percentage
    public double getGrossMarginPercentage() {
        if (unitPrice != null && unitPrice.doubleValue() > 0 && costPrice != null) {
            double margin = unitPrice.doubleValue() - costPrice.doubleValue();
            return (margin / unitPrice.doubleValue()) * 100.0;
        }
        return 0.0;
    }

    // Stock Status Classifier for UI Badges
    public String getStockStatus() {
        if (currentStock <= 0) {
            return "OUT_OF_STOCK";
        } else if (currentStock <= safetyStock) {
            return "LOW_STOCK";
        } else if (currentStock > (safetyStock * 4)) {
            return "OVERSTOCK";
        }
        return "OPTIMAL";
    }

    // Getters and Setters
    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
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

    public BigDecimal getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(BigDecimal costPrice) {
        this.costPrice = costPrice;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public int getCurrentStock() {
        return currentStock;
    }

    public void setCurrentStock(int currentStock) {
        this.currentStock = currentStock;
    }

    public int getSafetyStock() {
        return safetyStock;
    }

    public void setSafetyStock(int safetyStock) {
        this.safetyStock = safetyStock;
    }

    public int getLeadTimeDays() {
        return leadTimeDays;
    }

    public void setLeadTimeDays(int leadTimeDays) {
        this.leadTimeDays = leadTimeDays;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Product{" +
                "productId=" + productId +
                ", sku='" + sku + '\'' +
                ", productName='" + productName + '\'' +
                ", currentStock=" + currentStock +
                ", safetyStock=" + safetyStock +
                '}';
    }
}
