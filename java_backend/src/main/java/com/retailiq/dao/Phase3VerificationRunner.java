package com.retailiq.dao;

import com.retailiq.model.InventoryLog;
import com.retailiq.model.Product;
import com.retailiq.model.SaleItem;
import com.retailiq.model.SaleTransaction;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Phase3VerificationRunner - Standalone verification script for Phase 3.
 * 
 * Verifies:
 * 1. ACID Transaction Execution: Multi-item sale, invoice creation, stock deduction, and audit log.
 * 2. ACID Rollback Protection: Preventing sale if stock is insufficient.
 * 3. Restock Audit Trail: Adding stock and writing PURCHASE_RESTOCK log.
 */
public class Phase3VerificationRunner {

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("      RetailIQ - Phase 3 Verification Test");
        System.out.println("=================================================\n");

        ProductDAO productDAO = new ProductDAO();
        SalesDAO salesDAO = new SalesDAO();
        InventoryLogDAO inventoryLogDAO = new InventoryLogDAO();

        try {
            // Step 1: Query initial state of Product 1 (Wireless Mouse)
            Product mouseBefore = productDAO.findById(1);
            Product keyboardBefore = productDAO.findById(2);
            System.out.println("1. Baseline Inventory State:");
            System.out.printf("   - [%s] %s | Stock: %d units%n", mouseBefore.getSku(), mouseBefore.getProductName(), mouseBefore.getCurrentStock());
            System.out.printf("   - [%s] %s | Stock: %d units%n", keyboardBefore.getSku(), keyboardBefore.getProductName(), keyboardBefore.getCurrentStock());

            // Step 2: Simulate multi-item Point of Sale Transaction
            System.out.println("\n2. Simulating Atomic POS Checkout (2 Mice + 1 Keyboard):");
            SaleTransaction tx = new SaleTransaction();
            tx.setUserId(1); // Admin user
            tx.setPaymentMethod("CARD");

            List<SaleItem> items = new ArrayList<>();
            items.add(new SaleItem(1, mouseBefore.getProductName(), 2, mouseBefore.getUnitPrice()));
            items.add(new SaleItem(2, keyboardBefore.getProductName(), 1, keyboardBefore.getUnitPrice()));

            String invoice = salesDAO.processSale(tx, items);
            System.out.println("   [PASS] Checkout completed successfully!");
            System.out.println("   -> Generated Invoice Number: " + invoice);

            // Step 3: Verify Stock Deductions
            Product mouseAfter = productDAO.findById(1);
            Product keyboardAfter = productDAO.findById(2);
            System.out.println("\n3. Verifying Stock Deductions in Database:");
            System.out.printf("   - Mouse Stock: %d -> %d (Expected: %d) -> %s%n",
                    mouseBefore.getCurrentStock(), mouseAfter.getCurrentStock(),
                    mouseBefore.getCurrentStock() - 2,
                    (mouseAfter.getCurrentStock() == mouseBefore.getCurrentStock() - 2 ? "[PASS]" : "[FAIL]"));
            System.out.printf("   - Keyboard Stock: %d -> %d (Expected: %d) -> %s%n",
                    keyboardBefore.getCurrentStock(), keyboardAfter.getCurrentStock(),
                    keyboardBefore.getCurrentStock() - 1,
                    (keyboardAfter.getCurrentStock() == keyboardBefore.getCurrentStock() - 1 ? "[PASS]" : "[FAIL]"));

            // Step 4: Verify Invoice Lookup and Items
            System.out.println("\n4. Verifying Saved Invoice Details & Line Items:");
            SaleTransaction savedTx = salesDAO.findByInvoice(invoice);
            if (savedTx != null && savedTx.getItems().size() == 2) {
                System.out.println("   [PASS] Found invoice with " + savedTx.getItems().size() + " line items.");
                System.out.printf("   - Cashier: %s | Total: $%.2f | Payment: %s%n", 
                        savedTx.getCashierName(), savedTx.getTotalAmount(), savedTx.getPaymentMethod());
            } else {
                System.err.println("   [FAIL] Could not verify invoice items.");
            }

            // Step 5: Test ACID Rollback Protection (Attempting to purchase 99,999 units)
            System.out.println("\n5. Testing ACID Rollback Protection (Over-demand simulation):");
            SaleTransaction invalidTx = new SaleTransaction();
            invalidTx.setUserId(1);
            invalidTx.setPaymentMethod("CASH");

            List<SaleItem> excessiveItems = new ArrayList<>();
            excessiveItems.add(new SaleItem(1, mouseBefore.getProductName(), 99999, mouseBefore.getUnitPrice()));

            try {
                salesDAO.processSale(invalidTx, excessiveItems);
                System.err.println("   [FAIL] Security flaw: Allowed sale exceeding physical inventory!");
            } catch (Exception expected) {
                System.out.println("   [PASS] Successfully blocked and rolled back transaction!");
                System.out.println("   -> Intercepted Reason: " + expected.getMessage());
                // Verify mouse stock was NOT altered
                Product mouseCheck = productDAO.findById(1);
                if (mouseCheck.getCurrentStock() == mouseAfter.getCurrentStock()) {
                    System.out.println("   [PASS] Stock remained completely untouched after rollback.");
                } else {
                    System.err.println("   [FAIL] Inconsistent stock state after rollback!");
                }
            }

            // Step 6: Test Restocking Workflow
            System.out.println("\n6. Testing Supplier Restock Workflow:");
            int restockQty = 15;
            boolean restockOk = inventoryLogDAO.restockProduct(1, 1, restockQty, "Verification Restock Batch PO-888");
            Product mouseRestocked = productDAO.findById(1);
            if (restockOk && mouseRestocked.getCurrentStock() == mouseAfter.getCurrentStock() + restockQty) {
                System.out.printf("   [PASS] Successfully restocked Product 1 by +%d units. (New Stock: %d)%n", 
                        restockQty, mouseRestocked.getCurrentStock());
            } else {
                System.err.println("   [FAIL] Restock failed or stock mismatch.");
            }

            // Step 7: Verify Audit Trail Logs
            System.out.println("\n7. Inspecting Latest Inventory Audit Logs:");
            List<InventoryLog> recentLogs = inventoryLogDAO.findRecent(3);
            for (InventoryLog log : recentLogs) {
                System.out.printf("   - [LOG #%d] %-16s | %-20s | Qty: %+3d | Stock After: %3d | Reason: %s%n",
                        log.getLogId(), log.getChangeType(), log.getProductName(), 
                        log.getQuantityChanged(), log.getStockAfterChange(), log.getReason());
            }

            System.out.println("\n=================================================");
            System.out.println("   Phase 3 POS, ACID Transactions & Audit Verified!");
            System.out.println("=================================================");

        } catch (Exception e) {
            System.err.println("[UNEXPECTED ERROR] " + e.getMessage());
            e.printStackTrace();
        }
    }
}
