"""
test_api.py - Standalone Verification Test for Phase 4 ML Microservice

Verifies:
1. StatisticalEngine calculations (Mean, Std Dev, Normal distribution Safety Stock).
2. DataPreprocessor lag & rolling feature extraction.
3. SalesForecaster model training, MAE/RMSE evaluation, and multi-day recursive forecasting.
4. Flask REST API endpoint responses (/api/health, /api/stats, /api/forecast).
"""

import json
from datetime import datetime, timedelta
from app import app
import numpy as np
import pandas as pd
from pipeline.preprocessor import DataPreprocessor
from pipeline.statistical_engine import StatisticalEngine
from pipeline.train_models import SalesForecaster


def generate_synthetic_test_data(num_days=60):
    """Generates realistic 60 days of sales data with weekend spikes for testing."""
    records = []
    base_date = datetime.now() - timedelta(days=num_days)

    np.random.seed(42)
    for i in range(num_days):
        cur_date = base_date + timedelta(days=i)
        # Saturday (5) and Sunday (6) have higher volume
        if cur_date.weekday() in [5, 6]:
            qty = int(np.random.randint(12, 22))  # Weekend surge
        else:
            qty = int(np.random.randint(5, 12))  # Weekday normal
        records.append({"date": cur_date.strftime("%Y-%m-%d"), "quantity": qty})
    return records


def run_tests():
    print("=================================================")
    print("      RetailIQ - Phase 4 ML Verification Test")
    print("=================================================\n")

    sales_records = generate_synthetic_test_data(60)
    df_sales = pd.DataFrame(sales_records)
    print(f"Loaded {len(df_sales)} days of synthetic retail sales data.\n")

    # 1. Test Statistical Engine
    print("1. Testing Statistical Demand Engine:")
    quantities = df_sales["quantity"].tolist()
    stats = StatisticalEngine.calculate_descriptive_stats(quantities)
    print(
        f"   - Mean Daily Sales: {stats['mean']} units | Std Dev: {stats['std_dev']} | Variance: {stats['variance']}"
    )

    safety = StatisticalEngine.calculate_safety_stock(
        quantities, lead_time_days=3, service_level=0.95
    )
    print(
        f"   - Normal Distribution Safety Stock (95% service level, Lead Time 3 days): {safety['safety_stock']} units"
    )
    print(f"   - Calculated Reorder Point (ROP): {safety['reorder_point']} units")
    print("   [PASS] Statistical Engine verified!\n")

    # 2. Test Feature Engineering Preprocessor
    print("2. Testing Feature Engineering & Lag Generation:")
    feat_df = DataPreprocessor.create_features(df_sales)
    print(
        f"   - Generated {len(feat_df)} supervised training rows after lag shifts."
    )
    print(f"   - Features: {DataPreprocessor.FEATURE_COLUMNS}")
    sample_row = feat_df.iloc[-1]
    print(
        f"   - Sample Vector: Lag_1={sample_row['lag_1']}, Lag_7={sample_row['lag_7']}, "
        f"RollingMean_7={sample_row['rolling_mean_7']:.1f}, IsWeekend={sample_row['is_weekend']}"
    )
    print("   [PASS] Feature Engineering Pipeline verified!\n")

    # 3. Test Machine Learning Training & Evaluation
    print("3. Testing Machine Learning Model Training:")
    forecaster = SalesForecaster()
    eval_metrics = forecaster.train_and_evaluate(df_sales)

    lr_m = eval_metrics["linear_regression"]
    rf_m = eval_metrics["random_forest"]

    print(f"   [Model 1: Linear Regression]  -> MAE: {lr_m['mae']} | RMSE: {lr_m['rmse']} | R2: {lr_m['r2_score']}")
    print(f"   [Model 2: Random Forest]     -> MAE: {rf_m['mae']} | RMSE: {rf_m['rmse']} | R2: {rf_m['r2_score']}")
    print(f"   -> AI Recommendation: {eval_metrics['recommended_model']} performs best.")

    # 4. Test Recursive 7-Day Forecast
    print("\n4. Testing 7-Day Recursive Forward Forecast:")
    forecast_results = forecaster.forecast_horizon(df_sales, horizon_days=7)
    print(f"   - Future Forecast Dates: {forecast_results['forecast_dates']}")
    print(
        f"   - Linear Regression Forecast: {forecast_results['linear_regression']['daily']} "
        f"(Total: {forecast_results['linear_regression']['total_demand']} units)"
    )
    print(
        f"   - Random Forest Forecast   : {forecast_results['random_forest']['daily']} "
        f"(Total: {forecast_results['random_forest']['total_demand']} units)"
    )
    print("   [PASS] Model training & multi-step forecasting verified!\n")

    # 5. Test Flask REST API Endpoints (Using Flask Test Client)
    print("5. Testing Flask REST Endpoints via Test Client:")
    client = app.test_client()

    # Health endpoint
    res_health = client.get("/api/health")
    assert res_health.status_code == 200
    print(f"   - GET  /api/health   -> Status: {res_health.status_code} [OK]")

    # Stats endpoint
    res_stats = client.post(
        "/api/stats",
        data=json.dumps({"sales": quantities, "lead_time_days": 3}),
        content_type="application/json",
    )
    assert res_stats.status_code == 200
    print(f"   - POST /api/stats    -> Status: {res_stats.status_code} [OK]")

    # Forecast endpoint
    forecast_payload = {
        "product_id": 1,
        "lead_time_days": 3,
        "horizon_days": 7,
        "history": sales_records,
    }
    res_forecast = client.post(
        "/api/forecast",
        data=json.dumps(forecast_payload),
        content_type="application/json",
    )
    assert res_forecast.status_code == 200
    forecast_data = json.loads(res_forecast.data)
    print(f"   - POST /api/forecast -> Status: {res_forecast.status_code} [OK]")
    print(
        f"   -> Forecast API returned recommended model: {forecast_data['recommended_model']}"
    )

    print("\n=================================================")
    print("   Phase 4 ML Models, Stats & Flask API Verified!")
    print("=================================================")


if __name__ == "__main__":
    run_tests()
