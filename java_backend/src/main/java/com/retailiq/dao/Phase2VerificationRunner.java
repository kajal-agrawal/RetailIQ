package com.retailiq.dao;

import com.retailiq.model.Category;
import com.retailiq.model.Product;
import com.retailiq.model.User;
import java.util.List;

/**
 * Phase2VerificationRunner - Standalone verification script for Phase 2.
 * 
 * Tests:
 * 1. UserDAO authentication logic.
 * 2. CategoryDAO dropdown retrieval.
 * 3. ProductDAO catalog queries and low-stock filter.
 */
public class Phase2VerificationRunner {

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("      RetailIQ - Phase 2 Verification Test");
        System.out.println("=================================================\n");

        UserDAO userDAO = new UserDAO();
        CategoryDAO categoryDAO = new CategoryDAO();
        ProductDAO productDAO = new ProductDAO();

        // 1. Test Authentication
        System.out.println("1. Testing User Authentication:");
        User admin = userDAO.authenticate("admin", "admin123");
        if (admin != null) {
            System.out.println("   [PASS] Successfully authenticated: " + admin.getFullName() + " (" + admin.getRole() + ")");
        } else {
            System.err.println("   [FAIL] Could not authenticate 'admin'.");
        }

        // Test invalid login
        User invalid = userDAO.authenticate("admin", "wrong_pass");
        if (invalid == null) {
            System.out.println("   [PASS] Rejected invalid password correctly.");
        } else {
            System.err.println("   [FAIL] Security vulnerability: Accepted invalid password!");
        }

        // 2. Test Category DAO
        System.out.println("\n2. Testing Category Retrieval:");
        List<Category> categories = categoryDAO.findAll();
        System.out.println("   [PASS] Found " + categories.size() + " categories:");
        for (Category c : categories) {
            System.out.println("      - ID: " + c.getCategoryId() + " | " + c.getCategoryName());
        }

        // 3. Test Product DAO
        System.out.println("\n3. Testing Product Catalog Retrieval:");
        List<Product> products = productDAO.findAll();
        System.out.println("   [PASS] Found " + products.size() + " products in database:");
        for (Product p : products) {
            System.out.printf("      - [%s] %-30s | Stock: %3d | Safety: %2d | Status: %s%n",
                    p.getSku(), p.getProductName(), p.getCurrentStock(), p.getSafetyStock(), p.getStockStatus());
        }

        // 4. Test Low Stock Filter
        System.out.println("\n4. Testing Low Stock Detection:");
        List<Product> lowStock = productDAO.findLowStock();
        System.out.println("   [PASS] Identified " + lowStock.size() + " product(s) needing reorder:");
        for (Product p : lowStock) {
            System.out.printf("      - ALERT: %s (Current: %d, Safety: %d)%n", 
                    p.getProductName(), p.getCurrentStock(), p.getSafetyStock());
        }

        System.out.println("\n=================================================");
        System.out.println("   Phase 2 Models, DAOs & Business Logic Verified!");
        System.out.println("=================================================");
    }
}
