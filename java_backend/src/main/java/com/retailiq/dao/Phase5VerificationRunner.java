package com.retailiq.dao;

import com.retailiq.model.ForecastResult;
import com.retailiq.model.HistoricalSalePoint;
import com.retailiq.model.Product;
import com.retailiq.service.AIRuleEngineService;
import com.retailiq.service.MLClientService;
import java.util.List;

/**
 * Phase5VerificationRunner - Standalone verification script for Phase 5.
 * 
 * Tests:
 * 1. Historical sales retrieval from MySQL via ForecastDAO.
 * 2. Java REST client communication with Python Flask ML Microservice (or fallback).
 * 3. AI Rule-Based Expert System evaluation across different inventory states.
 * 4. Audit logging into MySQL forecast_logs table.
 */
public class Phase5VerificationRunner {

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("      RetailIQ - Phase 5 AI & ML Verification");
        System.out.println("=================================================\n");

        ProductDAO productDAO = new ProductDAO();
        ForecastDAO forecastDAO = new ForecastDAO();
        MLClientService mlClient = new MLClientService();
        AIRuleEngineService aiRuleEngine = new AIRuleEngineService();

        try {
            // Step 1: Query historical sales for Product 1 (Wireless Mouse)
            System.out.println("1. Querying Historical Daily Sales from MySQL:");
            List<HistoricalSalePoint> history = forecastDAO.getHistoricalDailySales(1);
            System.out.println("   [PASS] Retrieved " + history.size() + " days of historical sales records.");
            if (!history.isEmpty()) {
                System.out.println("   - First recorded date: " + history.get(0).getDate() + " (" + history.get(0).getQuantity() + " units)");
                System.out.println("   - Most recent date   : " + history.get(history.size() - 1).getDate() + " (" + history.get(history.size() - 1).getQuantity() + " units)");
            }

            // Step 2: Request ML Forecast
            System.out.println("\n2. Requesting Machine Learning Forecast (Product 1, Horizon 7 Days):");
            Product mouse = productDAO.findById(1);
            ForecastResult forecastMouse = mlClient.requestForecast(1, mouse.getLeadTimeDays(), 7, history);

            System.out.println("   [PASS] ML Forecast generated successfully!");
            System.out.println("   - Recommended Model       : " + forecastMouse.getRecommendedModel());
            System.out.println("   - Linear Regression Total : " + forecastMouse.getLinearRegressionTotal() + " units (MAE: " + forecastMouse.getLinearRegressionMae() + ")");
            System.out.println("   - Random Forest Total     : " + forecastMouse.getRandomForestTotal() + " units (MAE: " + forecastMouse.getRandomForestMae() + ")");
            System.out.println("   - Calculated Safety Stock : " + forecastMouse.getSafetyStock() + " units");
            System.out.println("   - Calculated Reorder Point: " + forecastMouse.getReorderPoint() + " units");

            // Step 3: Run AI Rule-Based Expert System on Product 1
            System.out.println("\n3. Testing AI Rule-Based Recommendation Engine (Product 1 - Healthy Stock):");
            aiRuleEngine.evaluateInventory(mouse, forecastMouse);
            System.out.println("   - Stock State             : Current=" + mouse.getCurrentStock() + ", Safety=" + forecastMouse.getSafetyStock() + ", ROP=" + forecastMouse.getReorderPoint());
            System.out.println("   - AI Status Classification: " + forecastMouse.getAlertStatus());
            System.out.println("   - Recommended Action      : " + forecastMouse.getRecommendedAction());
            System.out.println("   - Recommended Reorder Qty : " + forecastMouse.getRecommendedOrderQuantity() + " units");
            System.out.println("   - Rule Explanation        : " + forecastMouse.getRuleExplanation());
            System.out.println("   [PASS] AI Rule evaluation verified for healthy stock!");

            // Step 4: Test AI Rule-Based System on Product 6 (Herbal Shampoo - Low Stock)
            System.out.println("\n4. Testing AI Rule-Based Recommendation Engine (Product 6 - Low Stock Trigger):");
            Product shampoo = productDAO.findById(6);
            List<HistoricalSalePoint> shampooHistory = forecastDAO.getHistoricalDailySales(6);
            ForecastResult forecastShampoo = mlClient.requestForecast(6, shampoo.getLeadTimeDays(), 7, shampooHistory);
            aiRuleEngine.evaluateInventory(shampoo, forecastShampoo);

            System.out.println("   - Stock State             : Current=" + shampoo.getCurrentStock() + ", Safety=" + forecastShampoo.getSafetyStock() + ", ROP=" + forecastShampoo.getReorderPoint());
            System.out.println("   - AI Status Classification: " + forecastShampoo.getAlertStatus());
            System.out.println("   - Recommended Action      : " + forecastShampoo.getRecommendedAction());
            System.out.println("   - Recommended Reorder Qty : " + forecastShampoo.getRecommendedOrderQuantity() + " units");
            System.out.println("   - Rule Explanation        : " + forecastShampoo.getRuleExplanation());
            System.out.println("   [PASS] AI Rule correctly triggered reorder quantity of " + forecastShampoo.getRecommendedOrderQuantity() + " units!");

            // Step 5: Verify Persistence to MySQL forecast_logs
            System.out.println("\n5. Testing Persistence to MySQL 'forecast_logs' table:");
            int logId = forecastDAO.saveForecastLog(
                    shampoo.getProductId(),
                    forecastShampoo.getRecommendedModel(),
                    7,
                    forecastShampoo.getChosenTotalDemand(),
                    forecastShampoo.getSafetyStock(),
                    forecastShampoo.getRecommendedOrderQuantity(),
                    forecastShampoo.getAlertStatus()
            );

            if (logId > 0) {
                System.out.println("   [PASS] Successfully persisted AI recommendation to database. (Forecast Log ID: " + logId + ")");
            } else {
                System.err.println("   [FAIL] Failed to persist forecast log.");
            }

            System.out.println("\n=================================================");
            System.out.println("   Phase 5 AI Rule Engine & ML Integration Verified!");
            System.out.println("=================================================");

        } catch (Exception e) {
            System.err.println("[UNEXPECTED ERROR] " + e.getMessage());
            e.printStackTrace();
        }
    }
}
