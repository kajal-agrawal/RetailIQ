"""
train_models.py - Linear Regression, Ridge, Lasso, and Random Forest Forecasting

Academic Principles Demonstrated (Machine Learning & Data Analytics Syllabus):
- Module II (Regression Analysis):
  1. Ordinary Least Squares (OLS) Linear Regression.
  2. Ridge Regression (L2 Regularization: penalizes large weights, handles multicollinear lags).
  3. Lasso Regression (L1 Regularization: enforces sparsity / automatic feature selection).
- Module IV (Ensemble Learning & Bias-Variance Tradeoff):
  4. Random Forest Regressor (Non-linear bagging ensemble of 100 decision trees).
- Evaluation Metrics: MAE, RMSE, and R2 on chronological train/test split.
"""

import numpy as np
import pandas as pd
from pipeline.preprocessor import DataPreprocessor
from sklearn.ensemble import RandomForestRegressor
from sklearn.linear_model import Lasso, LinearRegression, Ridge
from sklearn.metrics import mean_absolute_error, mean_squared_error, r2_score


class SalesForecaster:
    """Trains and compares 4 regression algorithms: OLS, Ridge, Lasso, and Random Forest."""

    def __init__(self):
        # 1. Ordinary Least Squares (OLS) Linear Regression
        self.lr_model = LinearRegression()

        # 2. Ridge Regression (L2 Regularization: alpha=1.0)
        self.ridge_model = Ridge(alpha=1.0, random_state=42)

        # 3. Lasso Regression (L1 Regularization: alpha=0.1)
        self.lasso_model = Lasso(alpha=0.1, max_iter=2000, random_state=42)

        # 4. Random Forest Regressor (Non-linear ensemble)
        self.rf_model = RandomForestRegressor(
            n_estimators=100,
            max_depth=6,
            min_samples_split=3,
            random_state=42,
        )

        self.is_trained = False
        self.evaluation_metrics = {}

    def train_and_evaluate(self, df_sales):
        """
        Trains all 4 models and computes comparative MAE, RMSE, and R2 metrics.
        """
        features_df = DataPreprocessor.create_features(df_sales)
        if len(features_df) < 15:
            raise ValueError(
                f"Insufficient historical data after lag feature extraction ({len(features_df)} rows). "
                "Need at least 30 days of daily sales."
            )

        X, y = DataPreprocessor.extract_X_y(features_df)

        # Chronological 80/20 train-test split (no shuffling to prevent lookahead leakage)
        split_idx = int(len(X) * 0.8)
        X_train, X_test = X.iloc[:split_idx], X.iloc[split_idx:]
        y_train, y_test = y.iloc[:split_idx], y.iloc[split_idx:]

        models = {
            "linear_regression": self.lr_model,
            "ridge_regression": self.ridge_model,
            "lasso_regression": self.lasso_model,
            "random_forest": self.rf_model,
        }

        self.evaluation_metrics = {}
        best_model_name = "random_forest"
        lowest_mae = float("inf")

        for name, model in models.items():
            model.fit(X_train, y_train)
            preds = model.predict(X_test)
            mae = float(mean_absolute_error(y_test, preds))
            rmse = float(np.sqrt(mean_squared_error(y_test, preds)))
            r2 = float(r2_score(y_test, preds))

            self.evaluation_metrics[name] = {
                "mae": round(mae, 2),
                "rmse": round(rmse, 2),
                "r2_score": round(r2, 2),
            }

            if mae < lowest_mae:
                lowest_mae = mae
                best_model_name = name

            # Retrain on 100% of available data for forward predictions
            model.fit(X, y)

        self.is_trained = True
        self.evaluation_metrics["recommended_model"] = (
            best_model_name.upper()
        )
        return self.evaluation_metrics

    def forecast_horizon(self, df_sales, horizon_days=7):
        """
        Executes recursive multi-step forecasting across the future horizon for all 4 models.
        """
        if not self.is_trained:
            self.train_and_evaluate(df_sales)

        history_base = list(df_sales["quantity"].values)
        dates = pd.to_datetime(df_sales["date"]).tolist()
        last_date = dates[-1]

        # Tracking histories for recursive step
        histories = {
            "linear_regression": list(history_base),
            "ridge_regression": list(history_base),
            "lasso_regression": list(history_base),
            "random_forest": list(history_base),
        }

        models = {
            "linear_regression": self.lr_model,
            "ridge_regression": self.ridge_model,
            "lasso_regression": self.lasso_model,
            "random_forest": self.rf_model,
        }

        daily_predictions = {
            "linear_regression": [],
            "ridge_regression": [],
            "lasso_regression": [],
            "random_forest": [],
        }

        forecast_dates = []

        for step in range(1, horizon_days + 1):
            next_date = last_date + pd.Timedelta(days=step)
            day_of_week = next_date.dayofweek
            is_weekend = 1 if day_of_week in [5, 6] else 0
            forecast_dates.append(next_date.strftime("%Y-%m-%d"))

            for model_name, model in models.items():
                h = histories[model_name]
                feat = [
                    h[-1],  # lag_1
                    h[-7] if len(h) >= 7 else h[-1],  # lag_7
                    h[-14] if len(h) >= 14 else h[-1],  # lag_14
                    float(np.mean(h[-7:])),  # rolling_mean_7
                    float(np.std(h[-7:], ddof=1)) if len(h) >= 7 else 0.0,
                    day_of_week,
                    is_weekend,
                ]
                pred = max(
                    0.0,
                    float(
                        model.predict(
                            pd.DataFrame(
                                [feat], columns=DataPreprocessor.FEATURE_COLUMNS
                            )
                        )[0]
                    ),
                )
                daily_predictions[model_name].append(round(pred, 1))
                h.append(pred)

        results = {
            "forecast_dates": forecast_dates,
            "recommended_model": self.evaluation_metrics.get(
                "recommended_model", "RANDOM_FOREST"
            ),
        }

        for model_name in models.keys():
            preds = daily_predictions[model_name]
            results[model_name] = {
                "daily": preds,
                "total_demand": round(float(sum(preds)), 1),
                "metrics": self.evaluation_metrics.get(model_name, {}),
            }

        return results
