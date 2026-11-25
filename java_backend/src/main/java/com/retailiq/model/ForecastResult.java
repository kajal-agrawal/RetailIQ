package com.retailiq.model;

import java.util.ArrayList;
import java.util.List;

/**
 * ForecastResult - Unified model capturing 4 ML regression models, advanced statistical dispersion,
 * Poisson probability risk, and AI Expert System recommendations.
 */
public class ForecastResult implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    private int productId;
    private int horizonDays;
    private List<String> forecastDates = new ArrayList<>();

    // Model 1: Ordinary Least Squares (OLS) Linear Regression
    private List<Double> linearRegressionDaily = new ArrayList<>();
    private double linearRegressionTotal;
    private double linearRegressionMae;
    private double linearRegressionRmse;
    private double linearRegressionR2;

    // Model 2: Ridge Regression (L2 Regularization)
    private List<Double> ridgeRegressionDaily = new ArrayList<>();
    private double ridgeRegressionTotal;
    private double ridgeRegressionMae;
    private double ridgeRegressionRmse;
    private double ridgeRegressionR2;

    // Model 3: Lasso Regression (L1 Regularization)
    private List<Double> lassoRegressionDaily = new ArrayList<>();
    private double lassoRegressionTotal;
    private double lassoRegressionMae;
    private double lassoRegressionRmse;
    private double lassoRegressionR2;

    // Model 4: Random Forest (Ensemble)
    private List<Double> randomForestDaily = new ArrayList<>();
    private double randomForestTotal;
    private double randomForestMae;
    private double randomForestRmse;
    private double randomForestR2;

    // Evaluation Winner
    private String recommendedModel; // "LINEAR_REGRESSION", "RIDGE_REGRESSION", "LASSO_REGRESSION", "RANDOM_FOREST"

    // Statistics & Probability Parameters
    private double meanDailyDemand;
    private double demandStdDev;
    private double kurtosis;
    private double coefficientOfVariation; // CV %
    private String demandStability;        // "STABLE", "MODERATE", "VOLATILE"
    private int safetyStock;
    private int reorderPoint;

    // Discrete Probability (Poisson Distribution)
    private double poissonStockoutRiskPercentage;
    private String poissonRiskLevel; // "CRITICAL", "ELEVATED", "LOW"

    // AI Rule-Based Recommendation Output
    private String alertStatus;             // 'CRITICAL_STOCKOUT', 'LOW_STOCK', 'OPTIMAL', 'OVERSTOCK'
    private String urgencyLevel;            // 'HIGH', 'MEDIUM', 'LOW', 'NONE'
    private String recommendedAction;
    private int recommendedOrderQuantity;
    private String ruleExplanation;

    public ForecastResult() {}

    public double getChosenTotalDemand() {
        if ("LINEAR_REGRESSION".equalsIgnoreCase(recommendedModel)) return linearRegressionTotal;
        if ("RIDGE_REGRESSION".equalsIgnoreCase(recommendedModel)) return ridgeRegressionTotal;
        if ("LASSO_REGRESSION".equalsIgnoreCase(recommendedModel)) return lassoRegressionTotal;
        return randomForestTotal > 0 ? randomForestTotal : linearRegressionTotal;
    }

    public List<Double> getChosenDailyForecast() {
        if ("LINEAR_REGRESSION".equalsIgnoreCase(recommendedModel)) return linearRegressionDaily;
        if ("RIDGE_REGRESSION".equalsIgnoreCase(recommendedModel)) return ridgeRegressionDaily;
        if ("LASSO_REGRESSION".equalsIgnoreCase(recommendedModel)) return lassoRegressionDaily;
        return (randomForestDaily != null && !randomForestDaily.isEmpty()) ? randomForestDaily : linearRegressionDaily;
    }

    public double getChosenMae() {
        if ("LINEAR_REGRESSION".equalsIgnoreCase(recommendedModel)) return linearRegressionMae;
        if ("RIDGE_REGRESSION".equalsIgnoreCase(recommendedModel)) return ridgeRegressionMae;
        if ("LASSO_REGRESSION".equalsIgnoreCase(recommendedModel)) return lassoRegressionMae;
        return randomForestMae;
    }

    // Getters and Setters
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public int getHorizonDays() { return horizonDays; }
    public void setHorizonDays(int horizonDays) { this.horizonDays = horizonDays; }

    public List<String> getForecastDates() { return forecastDates; }
    public void setForecastDates(List<String> forecastDates) { this.forecastDates = forecastDates; }

    public List<Double> getLinearRegressionDaily() { return linearRegressionDaily; }
    public void setLinearRegressionDaily(List<Double> linearRegressionDaily) { this.linearRegressionDaily = linearRegressionDaily; }

    public double getLinearRegressionTotal() { return linearRegressionTotal; }
    public void setLinearRegressionTotal(double linearRegressionTotal) { this.linearRegressionTotal = linearRegressionTotal; }

    public double getLinearRegressionMae() { return linearRegressionMae; }
    public void setLinearRegressionMae(double linearRegressionMae) { this.linearRegressionMae = linearRegressionMae; }

    public double getLinearRegressionRmse() { return linearRegressionRmse; }
    public void setLinearRegressionRmse(double linearRegressionRmse) { this.linearRegressionRmse = linearRegressionRmse; }

    public double getLinearRegressionR2() { return linearRegressionR2; }
    public void setLinearRegressionR2(double linearRegressionR2) { this.linearRegressionR2 = linearRegressionR2; }

    public List<Double> getRidgeRegressionDaily() { return ridgeRegressionDaily; }
    public void setRidgeRegressionDaily(List<Double> ridgeRegressionDaily) { this.ridgeRegressionDaily = ridgeRegressionDaily; }

    public double getRidgeRegressionTotal() { return ridgeRegressionTotal; }
    public void setRidgeRegressionTotal(double ridgeRegressionTotal) { this.ridgeRegressionTotal = ridgeRegressionTotal; }

    public double getRidgeRegressionMae() { return ridgeRegressionMae; }
    public void setRidgeRegressionMae(double ridgeRegressionMae) { this.ridgeRegressionMae = ridgeRegressionMae; }

    public double getRidgeRegressionRmse() { return ridgeRegressionRmse; }
    public void setRidgeRegressionRmse(double ridgeRegressionRmse) { this.ridgeRegressionRmse = ridgeRegressionRmse; }

    public double getRidgeRegressionR2() { return ridgeRegressionR2; }
    public void setRidgeRegressionR2(double ridgeRegressionR2) { this.ridgeRegressionR2 = ridgeRegressionR2; }

    public List<Double> getLassoRegressionDaily() { return lassoRegressionDaily; }
    public void setLassoRegressionDaily(List<Double> lassoRegressionDaily) { this.lassoRegressionDaily = lassoRegressionDaily; }

    public double getLassoRegressionTotal() { return lassoRegressionTotal; }
    public void setLassoRegressionTotal(double lassoRegressionTotal) { this.lassoRegressionTotal = lassoRegressionTotal; }

    public double getLassoRegressionMae() { return lassoRegressionMae; }
    public void setLassoRegressionMae(double lassoRegressionMae) { this.lassoRegressionMae = lassoRegressionMae; }

    public double getLassoRegressionRmse() { return lassoRegressionRmse; }
    public void setLassoRegressionRmse(double lassoRegressionRmse) { this.lassoRegressionRmse = lassoRegressionRmse; }

    public double getLassoRegressionR2() { return lassoRegressionR2; }
    public void setLassoRegressionR2(double lassoRegressionR2) { this.lassoRegressionR2 = lassoRegressionR2; }

    public List<Double> getRandomForestDaily() { return randomForestDaily; }
    public void setRandomForestDaily(List<Double> randomForestDaily) { this.randomForestDaily = randomForestDaily; }

    public double getRandomForestTotal() { return randomForestTotal; }
    public void setRandomForestTotal(double randomForestTotal) { this.randomForestTotal = randomForestTotal; }

    public double getRandomForestMae() { return randomForestMae; }
    public void setRandomForestMae(double randomForestMae) { this.randomForestMae = randomForestMae; }

    public double getRandomForestRmse() { return randomForestRmse; }
    public void setRandomForestRmse(double randomForestRmse) { this.randomForestRmse = randomForestRmse; }

    public double getRandomForestR2() { return randomForestR2; }
    public void setRandomForestR2(double randomForestR2) { this.randomForestR2 = randomForestR2; }

    public String getRecommendedModel() { return recommendedModel; }
    public void setRecommendedModel(String recommendedModel) { this.recommendedModel = recommendedModel; }

    public double getMeanDailyDemand() { return meanDailyDemand; }
    public void setMeanDailyDemand(double meanDailyDemand) { this.meanDailyDemand = meanDailyDemand; }

    public double getDemandStdDev() { return demandStdDev; }
    public void setDemandStdDev(double demandStdDev) { this.demandStdDev = demandStdDev; }

    public double getKurtosis() { return kurtosis; }
    public void setKurtosis(double kurtosis) { this.kurtosis = kurtosis; }

    public double getCoefficientOfVariation() { return coefficientOfVariation; }
    public void setCoefficientOfVariation(double coefficientOfVariation) { this.coefficientOfVariation = coefficientOfVariation; }

    public String getDemandStability() { return demandStability; }
    public void setDemandStability(String demandStability) { this.demandStability = demandStability; }

    public int getSafetyStock() { return safetyStock; }
    public void setSafetyStock(int safetyStock) { this.safetyStock = safetyStock; }

    public int getReorderPoint() { return reorderPoint; }
    public void setReorderPoint(int reorderPoint) { this.reorderPoint = reorderPoint; }

    public double getPoissonStockoutRiskPercentage() { return poissonStockoutRiskPercentage; }
    public void setPoissonStockoutRiskPercentage(double poissonStockoutRiskPercentage) { this.poissonStockoutRiskPercentage = poissonStockoutRiskPercentage; }

    public String getPoissonRiskLevel() { return poissonRiskLevel; }
    public void setPoissonRiskLevel(String poissonRiskLevel) { this.poissonRiskLevel = poissonRiskLevel; }

    public String getAlertStatus() { return alertStatus; }
    public void setAlertStatus(String alertStatus) { this.alertStatus = alertStatus; }

    public String getUrgencyLevel() { return urgencyLevel; }
    public void setUrgencyLevel(String urgencyLevel) { this.urgencyLevel = urgencyLevel; }

    public String getRecommendedAction() { return recommendedAction; }
    public void setRecommendedAction(String recommendedAction) { this.recommendedAction = recommendedAction; }

    public int getRecommendedOrderQuantity() { return recommendedOrderQuantity; }
    public void setRecommendedOrderQuantity(int recommendedOrderQuantity) { this.recommendedOrderQuantity = recommendedOrderQuantity; }

    public String getRuleExplanation() { return ruleExplanation; }
    public void setRuleExplanation(String ruleExplanation) { this.ruleExplanation = ruleExplanation; }
}
