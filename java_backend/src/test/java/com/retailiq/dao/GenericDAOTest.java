package com.retailiq.dao;

import com.retailiq.exception.InsufficientStockException;
import com.retailiq.exception.ProductNotFoundException;
import com.retailiq.model.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GenericDAOTest - Automated unit test verifying the GenericDAO<T> interface and Custom Exceptions.
 * 
 * Academic Principle:
 * - Module II: Interfaces (Defining, abstract methods, default methods, constants).
 * - Module III: User-defined custom exceptions (InsufficientStockException, ProductNotFoundException).
 */
public class GenericDAOTest {

    @Test
    @DisplayName("Module II: GenericDAO interface static constants and default methods")
    public void testGenericDAOInterfaceFeatures() {
        // Test static method from interface
        assertEquals(50, GenericDAO.getDefaultLimit(), "Interface static method should return default limit 50");
        assertEquals(50, GenericDAO.DEFAULT_QUERY_LIMIT, "Interface constant should be accessible");

        // Test interface polymorphism via concrete implementation
        GenericDAO<Product> dao = new ProductDAO();
        assertNotNull(dao, "ProductDAO must implement GenericDAO<Product>");
    }

    @Test
    @DisplayName("Module III: User-defined InsufficientStockException and ProductNotFoundException")
    public void testCustomExceptions() {
        InsufficientStockException stockEx = new InsufficientStockException(1, "Wireless Mouse", 50, 10);
        assertEquals(1, stockEx.getProductId());
        assertEquals(50, stockEx.getRequestedQuantity());
        assertEquals(10, stockEx.getAvailableQuantity());
        assertTrue(stockEx.getMessage().contains("Insufficient stock for 'Wireless Mouse'"));

        ProductNotFoundException notFoundEx = new ProductNotFoundException(999);
        assertEquals(999, notFoundEx.getProductId());
        assertTrue(notFoundEx.getMessage().contains("ID 999 was not found"));
    }
}
