package com.retailiq.service;

import com.retailiq.model.ForecastResult;
import com.retailiq.model.Product;

/**
 * AIRuleEngineService - Knowledge-Based IF-THEN Rule Expert System.
 * 
 * Academic Principle:
 * - Artificial Intelligence (Expert Systems / Production Rules).
 * - Forward-chaining inference engine applying domain heuristics to inventory metrics.
 * 
 * Rules Evaluated:
 * - Rule 1 (Critical Stockout Imminent): Current_Stock <= Safety_Stock
 * - Rule 2 (Reorder Threshold Triggered): Current_Stock <= Reorder_Point
 * - Rule 3 (Optimal Inventory Health): Reorder_Point < Current_Stock <= (2.5 * Reorder_Point)
 * - Rule 4 (Overstock / Excess Capital): Current_Stock > (2.5 * Reorder_Point)
 */
public class AIRuleEngineService {

    /**
     * Evaluates product stock and demand forecast to produce actionable AI recommendations.
     */
    public void evaluateInventory(Product product, ForecastResult forecast) {
        int currentStock = product.getCurrentStock();
        int safetyStock = forecast.getSafetyStock() > 0 ? forecast.getSafetyStock() : product.getSafetyStock();
        int reorderPoint = forecast.getReorderPoint() > 0 ? forecast.getReorderPoint() : (safetyStock * 2);
        double forecastedDemand = forecast.getChosenTotalDemand();

        if (currentStock <= safetyStock) {
            // Critical Stockout Imminent
            int recommendedQty = (int) Math.ceil((forecastedDemand + safetyStock) - currentStock);
            forecast.setAlertStatus("CRITICAL_STOCKOUT");
            forecast.setUrgencyLevel("HIGH");
            forecast.setRecommendedAction("EXPEDITE_REORDER");
            forecast.setRecommendedOrderQuantity(Math.max(10, recommendedQty));
            forecast.setRuleExplanation(
                "Critical Stockout Alert: Current inventory (" + currentStock + 
                " units) is at or below the safety threshold (" + safetyStock + 
                " units). Immediate supplier replenishment recommended to prevent store stockout."
            );

        } else if (currentStock <= reorderPoint) {
            // Reorder Threshold Triggered
            int recommendedQty = (int) Math.ceil((forecastedDemand + safetyStock) - currentStock);
            forecast.setAlertStatus("LOW_STOCK");
            forecast.setUrgencyLevel("MEDIUM");
            forecast.setRecommendedAction("PLACE_STANDARD_ORDER");
            forecast.setRecommendedOrderQuantity(Math.max(5, recommendedQty));
            forecast.setRuleExplanation(
                "Low Stock Advisory: Current inventory (" + currentStock + 
                " units) has crossed the reorder point (" + reorderPoint + 
                " units). Place standard replenishment order."
            );

        } else if (currentStock <= (int)(2.5 * reorderPoint)) {
            // Optimal Inventory Balance
            forecast.setAlertStatus("OPTIMAL");
            forecast.setUrgencyLevel("NONE");
            forecast.setRecommendedAction("MAINTAIN_LEVELS");
            forecast.setRecommendedOrderQuantity(0);
            forecast.setRuleExplanation(
                "Optimal Inventory Level: Current inventory (" + currentStock + 
                " units) is comfortably balanced against projected demand (" + (int)forecastedDemand + 
                " units). No replenishment required."
            );

        } else {
            // Overstock & Capital Lockup
            forecast.setAlertStatus("OVERSTOCK");
            forecast.setUrgencyLevel("LOW");
            forecast.setRecommendedAction("HALT_PURCHASE_AND_DISCOUNT");
            forecast.setRecommendedOrderQuantity(0);
            forecast.setRuleExplanation(
                "Overstock Advisory: Current inventory (" + currentStock + 
                " units) exceeds operational targets. Recommend holding purchases and reviewing promotional discounts."
            );
        }
    }
}
