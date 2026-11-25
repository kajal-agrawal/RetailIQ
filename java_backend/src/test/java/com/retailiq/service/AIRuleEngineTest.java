package com.retailiq.service;

import com.retailiq.model.ForecastResult;
import com.retailiq.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AIRuleEngineTest - Automated unit tests for the Knowledge-Based Expert System.
 * 
 * Tests the complete truth table of production rules:
 * - Rule 1: Critical Stockout Imminent
 * - Rule 2: Reorder Threshold Triggered
 * - Rule 3: Optimal Inventory Balance
 * - Rule 4: Overstock & Capital Lockup
 */
public class AIRuleEngineTest {

    private AIRuleEngineService ruleEngine;

    @BeforeEach
    public void setUp() {
        ruleEngine = new AIRuleEngineService();
    }

    @Test
    @DisplayName("Rule 1: Should trigger CRITICAL_STOCKOUT when current stock <= safety stock")
    public void testRule1CriticalStockout() {
        Product product = new Product();
        product.setCurrentStock(5);
        product.setSafetyStock(10);

        ForecastResult forecast = new ForecastResult();
        forecast.setSafetyStock(10);
        forecast.setReorderPoint(25);
        forecast.setLinearRegressionTotal(30.0);
        forecast.setRecommendedModel("LINEAR_REGRESSION");

        ruleEngine.evaluateInventory(product, forecast);

        assertEquals("CRITICAL_STOCKOUT", forecast.getAlertStatus(), "Status must be CRITICAL_STOCKOUT");
        assertEquals("HIGH", forecast.getUrgencyLevel(), "Urgency must be HIGH");
        assertEquals("EXPEDITE_REORDER", forecast.getRecommendedAction(), "Action must be EXPEDITE_REORDER");
        assertTrue(forecast.getRecommendedOrderQuantity() > 0, "Must recommend positive order quantity");
        assertTrue(forecast.getRuleExplanation().contains("Rule 1"), "Explanation must reference Rule 1");
    }

    @Test
    @DisplayName("Rule 2: Should trigger LOW_STOCK when safety stock < current stock <= reorder point")
    public void testRule2LowStock() {
        Product product = new Product();
        product.setCurrentStock(15);
        product.setSafetyStock(10);

        ForecastResult forecast = new ForecastResult();
        forecast.setSafetyStock(10);
        forecast.setReorderPoint(25);
        forecast.setLinearRegressionTotal(30.0);
        forecast.setRecommendedModel("LINEAR_REGRESSION");

        ruleEngine.evaluateInventory(product, forecast);

        assertEquals("LOW_STOCK", forecast.getAlertStatus(), "Status must be LOW_STOCK");
        assertEquals("MEDIUM", forecast.getUrgencyLevel(), "Urgency must be MEDIUM");
        assertEquals("PLACE_STANDARD_ORDER", forecast.getRecommendedAction(), "Action must be PLACE_STANDARD_ORDER");
        assertTrue(forecast.getRecommendedOrderQuantity() > 0, "Must recommend positive reorder quantity");
        assertTrue(forecast.getRuleExplanation().contains("Rule 2"), "Explanation must reference Rule 2");
    }

    @Test
    @DisplayName("Rule 3: Should trigger OPTIMAL when ROP < current stock <= 2.5 * ROP")
    public void testRule3OptimalInventory() {
        Product product = new Product();
        product.setCurrentStock(40);
        product.setSafetyStock(10);

        ForecastResult forecast = new ForecastResult();
        forecast.setSafetyStock(10);
        forecast.setReorderPoint(25);
        forecast.setLinearRegressionTotal(30.0);
        forecast.setRecommendedModel("LINEAR_REGRESSION");

        ruleEngine.evaluateInventory(product, forecast);

        assertEquals("OPTIMAL", forecast.getAlertStatus(), "Status must be OPTIMAL");
        assertEquals("NONE", forecast.getUrgencyLevel(), "Urgency must be NONE");
        assertEquals("MAINTAIN_LEVELS", forecast.getRecommendedAction(), "Action must be MAINTAIN_LEVELS");
        assertEquals(0, forecast.getRecommendedOrderQuantity(), "Recommended order quantity must be zero for optimal stock");
        assertTrue(forecast.getRuleExplanation().contains("Rule 3"), "Explanation must reference Rule 3");
    }

    @Test
    @DisplayName("Rule 4: Should trigger OVERSTOCK when current stock > 2.5 * ROP")
    public void testRule4Overstock() {
        Product product = new Product();
        product.setCurrentStock(100); // 100 > 2.5 * 25 (62.5)
        product.setSafetyStock(10);

        ForecastResult forecast = new ForecastResult();
        forecast.setSafetyStock(10);
        forecast.setReorderPoint(25);
        forecast.setLinearRegressionTotal(30.0);
        forecast.setRecommendedModel("LINEAR_REGRESSION");

        ruleEngine.evaluateInventory(product, forecast);

        assertEquals("OVERSTOCK", forecast.getAlertStatus(), "Status must be OVERSTOCK");
        assertEquals("LOW", forecast.getUrgencyLevel(), "Urgency must be LOW");
        assertEquals("HALT_PURCHASE_AND_DISCOUNT", forecast.getRecommendedAction(), "Action must be HALT_PURCHASE_AND_DISCOUNT");
        assertEquals(0, forecast.getRecommendedOrderQuantity(), "Recommended order quantity must be zero for overstock");
        assertTrue(forecast.getRuleExplanation().contains("Rule 4"), "Explanation must reference Rule 4");
    }
}
