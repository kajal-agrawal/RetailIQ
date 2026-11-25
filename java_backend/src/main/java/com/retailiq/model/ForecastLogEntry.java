package com.retailiq.model;

import java.sql.Timestamp;

/**
 * ForecastLogEntry - Model representing historical AI forecast and recommendation records.
 */
public class ForecastLogEntry implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    private int forecastId;
    private int productId;
    private String sku;
    private String productName;
    private String modelUsed;
    private int horizonDays;
    private double predictedSales;
    private double safetyStock;
    private int recommendedReorderQty;
    private String alertStatus;
    private Timestamp generatedAt;

    public ForecastLogEntry() {}

    public int getForecastId() {
        return forecastId;
    }

    public void setForecastId(int forecastId) {
        this.forecastId = forecastId;
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

    public String getModelUsed() {
        return modelUsed;
    }

    public void setModelUsed(String modelUsed) {
        this.modelUsed = modelUsed;
    }

    public int getHorizonDays() {
        return horizonDays;
    }

    public void setHorizonDays(int horizonDays) {
        this.horizonDays = horizonDays;
    }

    public double getPredictedSales() {
        return predictedSales;
    }

    public void setPredictedSales(double predictedSales) {
        this.predictedSales = predictedSales;
    }

    public double getSafetyStock() {
        return safetyStock;
    }

    public void setSafetyStock(double safetyStock) {
        this.safetyStock = safetyStock;
    }

    public int getRecommendedReorderQty() {
        return recommendedReorderQty;
    }

    public void setRecommendedReorderQty(int recommendedReorderQty) {
        this.recommendedReorderQty = recommendedReorderQty;
    }

    public String getAlertStatus() {
        return alertStatus;
    }

    public void setAlertStatus(String alertStatus) {
        this.alertStatus = alertStatus;
    }

    public Timestamp getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(Timestamp generatedAt) {
        this.generatedAt = generatedAt;
    }
}
