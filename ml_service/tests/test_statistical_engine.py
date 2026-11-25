"""
test_statistical_engine.py - Automated PyTest suite for StatisticalEngine
"""

import os
import sys
import numpy as np
import pytest

sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))
from pipeline.statistical_engine import StatisticalEngine


def test_descriptive_statistics_calculation():
    sales = [10, 12, 14, 16, 18]
    stats = StatisticalEngine.calculate_descriptive_stats(sales)

    assert stats["mean"] == 14.0
    assert stats["min"] == 10.0
    assert stats["max"] == 18.0
    assert stats["median"] == 14.0
    assert stats["variance"] == 10.0
    assert stats["std_dev"] == pytest.approx(np.sqrt(10.0), 0.01)

    # Verify syllabus additions: Kurtosis & Coefficient of Variation
    assert "kurtosis" in stats
    assert "coefficient_of_variation" in stats
    assert "demand_stability" in stats
    # CV = (std / mean) * 100 = (3.162 / 14.0) * 100 = 22.59% -> STABLE (< 25%)
    assert stats["coefficient_of_variation"] == pytest.approx(22.59, 0.1)
    assert stats["demand_stability"] == "STABLE"


def test_poisson_stockout_probability():
    # Mean daily demand = 10 units, Lead time = 1 day, Current stock = 8 units
    # Stockout is likely because stock (8) < mean (10)
    res = StatisticalEngine.calculate_poisson_stockout_risk(
        mean_daily_demand=10.0, current_stock=8, lead_time_days=1
    )

    assert "stockout_risk_percentage" in res
    assert "risk_level" in res
    assert res["lambda"] == 10.0
    assert res["stockout_risk_percentage"] > 50.0  # High risk of stockout
    assert res["risk_level"] in ["CRITICAL", "ELEVATED"]

    # When current stock is plenty (e.g. 50 units for mean 10), stockout risk should be near 0%
    safe_res = StatisticalEngine.calculate_poisson_stockout_risk(
        mean_daily_demand=10.0, current_stock=50, lead_time_days=1
    )
    assert safe_res["stockout_risk_percentage"] == 0.0
    assert safe_res["risk_level"] == "LOW"


def test_safety_stock_analytical():
    sales = [6, 14, 10, 10]
    res = StatisticalEngine.calculate_safety_stock(
        sales, lead_time_days=4, service_level=0.95
    )

    assert "safety_stock" in res
    assert "reorder_point" in res
    assert res["z_score"] == 1.65
    assert res["safety_stock"] > 0
    assert res["reorder_point"] > res["safety_stock"]
