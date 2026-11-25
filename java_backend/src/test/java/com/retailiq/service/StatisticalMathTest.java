package com.retailiq.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * StatisticalMathTest - Automated tests verifying statistical inventory formulas.
 * 
 * Verifies:
 * - Safety Stock: SS = Z * sigma * sqrt(L)
 * - Reorder Point: ROP = (mu * L) + SS
 */
public class StatisticalMathTest {

    @Test
    @DisplayName("Normal Distribution Safety Stock calculation with known analytical parameters")
    public void testSafetyStockAnalyticalCalculation() {
        double meanDemand = 10.0;
        double stdDev = 4.0;
        int leadTimeDays = 4; // sqrt(4) = 2.0
        double zScore = 1.65; // 95% service level

        // Analytical formulation: SS = 1.65 * 4.0 * 2.0 = 13.2 -> ceil = 14
        double exactSS = zScore * stdDev * Math.sqrt(leadTimeDays);
        int computedSS = (int) Math.ceil(exactSS);

        assertEquals(14, computedSS, "Calculated Safety Stock must equal 14 units");

        // Analytical ROP: (10.0 * 4) + 13.2 = 53.2 -> ceil = 54
        int computedROP = (int) Math.ceil((meanDemand * leadTimeDays) + exactSS);
        assertEquals(54, computedROP, "Calculated ROP must equal 54 units");
    }

    @Test
    @DisplayName("Edge Case: Zero variance and lead time 1 day")
    public void testZeroVarianceEdgeCase() {
        double meanDemand = 5.0;
        double stdDev = 0.0; // Deterministic demand
        int leadTimeDays = 1;
        double zScore = 1.65;

        double ss = zScore * stdDev * Math.sqrt(leadTimeDays);
        assertEquals(0.0, ss, "Safety stock with 0 variance must be 0");

        int rop = (int) Math.ceil((meanDemand * leadTimeDays) + ss);
        assertEquals(5, rop, "ROP with 0 variance should equal mean demand * lead time");
    }
}
