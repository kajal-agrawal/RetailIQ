"""
statistical_engine.py - Statistical Demand & Probability Engine for RetailIQ

Academic Principles Demonstrated (Statistics & Probability Syllabus):
- Module 2 (Location & Dispersion): Mean, Variance, Standard Deviation, Skewness, Kurtosis,
  Coefficient of Variation (CV = sigma / mu * 100).
- Module 2 (Normal Distribution): Z-score Safety Stock (SS = Z * sigma * sqrt(L)).
- Module 3 (Discrete Probability): Poisson Distribution for daily item demand and stockout risk:
  P(Stockout) = P(X > S) = 1 - sum_{k=0}^S (lambda^k * e^-lambda / k!)
"""

import math
import numpy as np
import pandas as pd


class StatisticalEngine:
    """Computes statistical metrics, dispersion, and probability formulas for retail sales demand."""

    @staticmethod
    def calculate_descriptive_stats(sales_series):
        """
        Computes summary statistics, dispersion, and distribution shape metrics.

        Metrics calculated:
        - Mean (mu)
        - Sample Variance (sigma^2) and Standard Deviation (sigma)
        - Median, Min, Max
        - Skewness (degree of asymmetry)
        - Kurtosis (fat-tail indicator of extreme demand spikes)
        - Coefficient of Variation (CV % = sigma / mu * 100): Demand predictability indicator.
        """
        series = pd.Series(sales_series, dtype=float)

        if len(series) == 0:
            return {
                "mean": 0.0,
                "variance": 0.0,
                "std_dev": 0.0,
                "median": 0.0,
                "min": 0.0,
                "max": 0.0,
                "skewness": 0.0,
                "kurtosis": 0.0,
                "coefficient_of_variation": 0.0,
                "demand_stability": "UNKNOWN",
            }

        mean = float(series.mean())
        variance = float(series.var(ddof=1)) if len(series) > 1 else 0.0
        std_dev = float(series.std(ddof=1)) if len(series) > 1 else 0.0
        median = float(series.median())
        min_val = float(series.min())
        max_val = float(series.max())
        skewness = float(series.skew()) if len(series) > 2 else 0.0
        kurtosis = float(series.kurt()) if len(series) > 3 else 0.0

        # Coefficient of Variation (CV %): measures relative demand dispersion
        cv = (std_dev / mean * 100.0) if mean > 0 else 0.0

        # Demand Stability Classification
        if cv < 25.0:
            stability = "STABLE"
        elif cv <= 50.0:
            stability = "MODERATE"
        else:
            stability = "VOLATILE"

        return {
            "mean": round(mean, 2),
            "variance": round(variance, 2),
            "std_dev": round(std_dev, 2),
            "median": round(median, 2),
            "min": round(min_val, 2),
            "max": round(max_val, 2),
            "skewness": round(skewness, 2),
            "kurtosis": round(kurtosis, 2),
            "coefficient_of_variation": round(cv, 2),
            "demand_stability": stability,
        }

    @staticmethod
    def calculate_poisson_stockout_risk(
        mean_daily_demand, current_stock, lead_time_days=1
    ):
        """
        Calculates the probability of a stockout using the discrete Poisson Distribution.

        Assumptions: Customer arrival & purchases follow a Poisson process with rate lambda = mu * lead_time.
        Formula:
            P(Demand <= CurrentStock) = sum_{k=0}^{CurrentStock} (lambda^k * e^(-lambda) / k!)
            P(Stockout) = 1 - P(Demand <= CurrentStock)

        Returns:
            dict: lambda_parameter, stockout_risk_percentage, risk_level ('CRITICAL', 'ELEVATED', 'LOW').
        """
        if mean_daily_demand <= 0:
            return {
                "lambda": 0.0,
                "stockout_risk_percentage": 0.0,
                "risk_level": "LOW",
            }

        rate_lambda = float(mean_daily_demand * max(1, lead_time_days))
        stock = max(0, int(current_stock))

        # Calculate cumulative Poisson probability P(X <= stock)
        # Using iterative log-safe summation to prevent overflow with large factorials
        cum_prob = 0.0
        term = math.exp(-rate_lambda)  # for k = 0
        cum_prob += term

        for k in range(1, stock + 1):
            term = term * (rate_lambda / k)
            cum_prob += term
            if cum_prob >= 1.0:
                cum_prob = 1.0
                break

        stockout_prob = max(0.0, min(1.0, 1.0 - cum_prob))
        stockout_pct = round(stockout_prob * 100.0, 1)

        if stockout_pct >= 60.0:
            risk_level = "CRITICAL"
        elif stockout_pct >= 20.0:
            risk_level = "ELEVATED"
        else:
            risk_level = "LOW"

        return {
            "lambda": round(rate_lambda, 2),
            "stockout_risk_percentage": stockout_pct,
            "risk_level": risk_level,
        }

    @staticmethod
    def calculate_safety_stock(
        sales_series, lead_time_days=3, service_level=0.95
    ):
        """Calculates dynamic Safety Stock (SS) and Reorder Point (ROP) via Normal Distribution."""
        series = pd.Series(sales_series, dtype=float)
        mean_demand = float(series.mean()) if len(series) > 0 else 0.0
        std_dev = float(series.std(ddof=1)) if len(series) > 1 else 0.0

        z_score_map = {0.90: 1.28, 0.95: 1.65, 0.98: 2.05, 0.99: 2.33}
        z = z_score_map.get(service_level, 1.65)

        lead_time = max(1, lead_time_days)
        safety_stock = z * std_dev * np.sqrt(lead_time)
        reorder_point = (mean_demand * lead_time) + safety_stock

        return {
            "z_score": z,
            "mean_daily_demand": round(mean_demand, 2),
            "demand_std_dev": round(std_dev, 2),
            "lead_time_days": lead_time,
            "safety_stock": int(np.ceil(safety_stock)),
            "reorder_point": int(np.ceil(reorder_point)),
        }

    @staticmethod
    def calculate_rolling_averages(sales_series, window=7):
        series = pd.Series(sales_series, dtype=float)
        rolling = series.rolling(window=window, min_periods=1).mean()
        return [round(float(val), 2) for val in rolling]
