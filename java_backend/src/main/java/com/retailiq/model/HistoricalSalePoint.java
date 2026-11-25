package com.retailiq.model;

/**
 * HistoricalSalePoint - Data transport model for daily sales records.
 * Used for serializing time-series sales to the Python Flask ML microservice.
 */
public class HistoricalSalePoint implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    private String date; // Format: "YYYY-MM-DD"
    private int quantity;

    public HistoricalSalePoint() {}

    public HistoricalSalePoint(String date, int quantity) {
        this.date = date;
        this.quantity = quantity;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "HistoricalSalePoint{" + "date='" + date + '\'' + ", quantity=" + quantity + '}';
    }
}
