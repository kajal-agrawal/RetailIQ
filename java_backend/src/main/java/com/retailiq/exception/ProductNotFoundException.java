package com.retailiq.exception;

/**
 * ProductNotFoundException - User-defined custom checked exception.
 * 
 * Thrown when an operation targets a product ID or SKU that does not exist.
 * 
 * Academic Principle:
 * - Module III (Exception Handling): Creating and using user-defined exceptions.
 */
public class ProductNotFoundException extends Exception {
    private static final long serialVersionUID = 1L;

    private final int productId;

    public ProductNotFoundException(int productId) {
        super(String.format("Product with ID %d was not found in the catalog.", productId));
        this.productId = productId;
    }

    public int getProductId() {
        return productId;
    }
}
