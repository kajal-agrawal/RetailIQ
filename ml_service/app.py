"""
app.py - RetailIQ Machine Learning & Statistical Flask REST API

Endpoints:
- GET  /api/health   -> Microservice status healthcheck
- POST /api/stats    -> Computes descriptive statistics, CV, Kurtosis, and Poisson risk
- POST /api/forecast -> Trains 4 models (OLS, Ridge, Lasso, Random Forest) and returns predictions
"""

import logging
from flask import Flask, jsonify, request
import pandas as pd
from pipeline.statistical_engine import StatisticalEngine
from pipeline.train_models import SalesForecaster

logging.basicConfig(
    level=logging.INFO, format="%(asctime)s - %(levelname)s - %(message)s"
)
logger = logging.getLogger("RetailIQ-ML")

app = Flask(__name__)


@app.route("/api/health", methods=["GET"])
def health_check():
    return jsonify(
        {
            "status": "healthy",
            "service": "RetailIQ Machine Learning & Statistical Engine",
            "version": "2.0.0",
            "algorithms": [
                "Linear Regression",
                "Ridge Regression (L2)",
                "Lasso Regression (L1)",
                "Random Forest",
            ],
        }
    )


@app.route("/api/stats", methods=["POST"])
def get_statistics():
    try:
        data = request.get_json(force=True)
        sales = data.get("sales", [])
        lead_time = int(data.get("lead_time_days", 3))
        service_level = float(data.get("service_level", 0.95))
        current_stock = int(data.get("current_stock", 10))

        if not sales or len(sales) == 0:
            return (
                jsonify({"status": "error", "message": "No sales data provided."}),
                400,
            )

        desc_stats = StatisticalEngine.calculate_descriptive_stats(sales)
        safety_stock = StatisticalEngine.calculate_safety_stock(
            sales, lead_time_days=lead_time, service_level=service_level
        )
        poisson_risk = StatisticalEngine.calculate_poisson_stockout_risk(
            desc_stats["mean"], current_stock, lead_time_days=lead_time
        )
        moving_avg = StatisticalEngine.calculate_rolling_averages(sales, window=7)

        return jsonify(
            {
                "status": "success",
                "descriptive_statistics": desc_stats,
                "safety_stock_parameters": safety_stock,
                "poisson_stockout_risk": poisson_risk,
                "moving_averages_7day": moving_avg,
            }
        )

    except Exception as e:
        logger.error(f"Error processing stats request: {str(e)}", exc_info=True)
        return jsonify({"status": "error", "message": str(e)}), 500


@app.route("/api/forecast", methods=["POST"])
def generate_forecast():
    try:
        data = request.get_json(force=True)
        product_id = data.get("product_id")
        lead_time_days = int(data.get("lead_time_days", 3))
        horizon_days = int(data.get("horizon_days", 7))
        current_stock = int(data.get("current_stock", 10))
        history = data.get("history", [])

        if not history or len(history) < 20:
            return (
                jsonify(
                    {
                        "status": "error",
                        "message": f"At least 20 daily sales data points required (received {len(history)}).",
                    }
                ),
                400,
            )

        df_sales = pd.DataFrame(history)
        if "date" not in df_sales.columns or "quantity" not in df_sales.columns:
            return (
                jsonify(
                    {
                        "status": "error",
                        "message": "History objects must contain 'date' and 'quantity' keys.",
                    }
                ),
                400,
            )

        # 1. Compute statistical parameters (Mean, StdDev, Kurtosis, CV, Poisson Risk)
        sales_quantities = df_sales["quantity"].tolist()
        desc_stats = StatisticalEngine.calculate_descriptive_stats(
            sales_quantities
        )
        safety_stock_data = StatisticalEngine.calculate_safety_stock(
            sales_quantities, lead_time_days=lead_time_days
        )
        poisson_risk = StatisticalEngine.calculate_poisson_stockout_risk(
            desc_stats["mean"], current_stock, lead_time_days=lead_time_days
        )

        # 2. Train and Forecast with all 4 ML Models
        forecaster = SalesForecaster()
        forecast_results = forecaster.forecast_horizon(
            df_sales, horizon_days=horizon_days
        )

        # 3. Assemble unified response
        response = {
            "status": "success",
            "product_id": product_id,
            "horizon_days": horizon_days,
            "forecast_dates": forecast_results["forecast_dates"],
            "models": {
                "linear_regression": forecast_results["linear_regression"],
                "ridge_regression": forecast_results["ridge_regression"],
                "lasso_regression": forecast_results["lasso_regression"],
                "random_forest": forecast_results["random_forest"],
            },
            "recommended_model": forecast_results["recommended_model"],
            "statistical_parameters": {
                "mean_daily_demand": safety_stock_data["mean_daily_demand"],
                "demand_std_dev": safety_stock_data["demand_std_dev"],
                "kurtosis": desc_stats["kurtosis"],
                "coefficient_of_variation": desc_stats[
                    "coefficient_of_variation"
                ],
                "demand_stability": desc_stats["demand_stability"],
                "calculated_safety_stock": safety_stock_data["safety_stock"],
                "reorder_point": safety_stock_data["reorder_point"],
                "poisson_stockout_risk_percentage": poisson_risk[
                    "stockout_risk_percentage"
                ],
                "poisson_risk_level": poisson_risk["risk_level"],
            },
        }

        logger.info(
            f"Generated forecast for product {product_id} across {horizon_days} days. "
            f"Recommended: {forecast_results['recommended_model']} | CV: {desc_stats['coefficient_of_variation']}%"
        )
        return jsonify(response)

    except Exception as e:
        logger.error(f"Forecasting error: {str(e)}", exc_info=True)
        return jsonify({"status": "error", "message": str(e)}), 500


if __name__ == "__main__":
    logger.info("Starting RetailIQ ML Microservice on http://127.0.0.1:5001")
    app.run(host="0.0.0.0", port=5001, debug=False)
