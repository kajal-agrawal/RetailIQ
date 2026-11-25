package com.retailiq.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * SaleTransaction - Entity Model representing a sales order header.
 * 
 * Demonstrates:
 * - Composition: A transaction "has-a" list of SaleItem objects.
 * - Encapsulation of transaction metadata.
 */
public class SaleTransaction implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    private int transactionId;
    private int userId;
    private String cashierName; // Joined from users table
    private String invoiceNumber;
    private Timestamp transactionDate;
    private BigDecimal totalAmount;
    private String paymentMethod; // 'CASH', 'CARD', 'UPI'
    private List<SaleItem> items = new ArrayList<>();

    public SaleTransaction() {}

    public SaleTransaction(int transactionId, int userId, String invoiceNumber, 
                           Timestamp transactionDate, BigDecimal totalAmount, String paymentMethod) {
        this.transactionId = transactionId;
        this.userId = userId;
        this.invoiceNumber = invoiceNumber;
        this.transactionDate = transactionDate;
        this.totalAmount = totalAmount;
        this.paymentMethod = paymentMethod;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getCashierName() {
        return cashierName;
    }

    public void setCashierName(String cashierName) {
        this.cashierName = cashierName;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public Timestamp getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(Timestamp transactionDate) {
        this.transactionDate = transactionDate;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public List<SaleItem> getItems() {
        return items;
    }

    public void setItems(List<SaleItem> items) {
        this.items = items;
    }

    public void addItem(SaleItem item) {
        this.items.add(item);
    }
}
