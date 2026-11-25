package com.retailiq.exception;

/**
 * InsufficientStockException - User-defined custom checked exception.
 * 
 * Thrown when an order or sale transaction requests more units
 * than are physically available in inventory.
 * 
 * Academic Principle:
 * - Module III (Exception Handling): Creating and using user-defined exceptions.
 */
public class InsufficientStockException extends Exception {
    private static final long serialVersionUID = 1L;

    private final int productId;
    private final int requestedQuantity;
    private final int availableQuantity;

    public InsufficientStockException(int productId, String productName, int requestedQuantity, int availableQuantity) {
        super(String.format("Insufficient stock for '%s' (ID: %d). Available: %d units, Requested: %d units.",
                productName, productId, availableQuantity, requestedQuantity));
        this.productId = productId;
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = availableQuantity;
    }

    public int getProductId() {
        return productId;
    }

    public int getRequestedQuantity() {
        return requestedQuantity;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }
}
