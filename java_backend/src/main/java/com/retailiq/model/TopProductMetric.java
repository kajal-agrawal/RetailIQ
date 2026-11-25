package com.retailiq.model;

import java.math.BigDecimal;

/**
 * TopProductMetric - Model representing fast-moving products by sales velocity and revenue.
 */
public class TopProductMetric implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    private int productId;
    private String sku;
    private String productName;
    private String categoryName;
    private int totalUnitsSold;
    private BigDecimal totalRevenue;
    private double grossMarginPercentage;

    public TopProductMetric() {}

    public TopProductMetric(int productId, String sku, String productName, String categoryName, 
                            int totalUnitsSold, BigDecimal totalRevenue, double grossMarginPercentage) {
        this.productId = productId;
        this.sku = sku;
        this.productName = productName;
        this.categoryName = categoryName;
        this.totalUnitsSold = totalUnitsSold;
        this.totalRevenue = totalRevenue;
        this.grossMarginPercentage = grossMarginPercentage;
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

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public int getTotalUnitsSold() {
        return totalUnitsSold;
    }

    public void setTotalUnitsSold(int totalUnitsSold) {
        this.totalUnitsSold = totalUnitsSold;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public double getGrossMarginPercentage() {
        return grossMarginPercentage;
    }

    public void setGrossMarginPercentage(double grossMarginPercentage) {
        this.grossMarginPercentage = grossMarginPercentage;
    }
}
