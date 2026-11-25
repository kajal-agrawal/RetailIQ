package com.retailiq.model;

import java.math.BigDecimal;

/**
 * CategoryRevenue - Data model for category revenue distribution analytics.
 */
public class CategoryRevenue implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    private String categoryName;
    private BigDecimal revenue;
    private double percentage;

    public CategoryRevenue() {}

    public CategoryRevenue(String categoryName, BigDecimal revenue, double percentage) {
        this.categoryName = categoryName;
        this.revenue = revenue;
        this.percentage = percentage;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }

    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }
}
