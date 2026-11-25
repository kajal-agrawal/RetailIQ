"""
test_ml_pipeline.py - Automated PyTest suite for Preprocessor and 4 ML Models
"""

from datetime import datetime, timedelta
import os
import sys
import numpy as np
import pandas as pd
import pytest

sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))
from pipeline.preprocessor import DataPreprocessor
from pipeline.train_models import SalesForecaster


@pytest.fixture
def sample_sales_df():
    records = []
    base_date = datetime(2026, 1, 1)
    np.random.seed(42)
    for i in range(50):
        d = base_date + timedelta(days=i)
        qty = 20 if d.weekday() in [5, 6] else 10
        records.append({"date": d.strftime("%Y-%m-%d"), "quantity": qty})
    return pd.DataFrame(records)


def test_preprocessor_lag_features(sample_sales_df):
    feat_df = DataPreprocessor.create_features(sample_sales_df)
    assert len(feat_df) == len(sample_sales_df) - 14
    for col in DataPreprocessor.FEATURE_COLUMNS:
        assert col in feat_df.columns


def test_four_model_training_and_regularization(sample_sales_df):
    forecaster = SalesForecaster()
    metrics = forecaster.train_and_evaluate(sample_sales_df)

    # Verify all 4 syllabus models are evaluated
    assert "linear_regression" in metrics
    assert "ridge_regression" in metrics
    assert "lasso_regression" in metrics
    assert "random_forest" in metrics
    assert "recommended_model" in metrics

    # Verify metrics for each
    for model_key in [
        "linear_regression",
        "ridge_regression",
        "lasso_regression",
        "random_forest",
    ]:
        assert metrics[model_key]["mae"] >= 0.0
        assert metrics[model_key]["rmse"] >= 0.0


def test_forecast_horizon_all_models(sample_sales_df):
    forecaster = SalesForecaster()
    horizon = 7
    result = forecaster.forecast_horizon(sample_sales_df, horizon_days=horizon)

    assert len(result["forecast_dates"]) == horizon
    assert len(result["linear_regression"]["daily"]) == horizon
    assert len(result["ridge_regression"]["daily"]) == horizon
    assert len(result["lasso_regression"]["daily"]) == horizon
    assert len(result["random_forest"]["daily"]) == horizon

    assert result["ridge_regression"]["total_demand"] > 0.0
    assert result["lasso_regression"]["total_demand"] > 0.0
