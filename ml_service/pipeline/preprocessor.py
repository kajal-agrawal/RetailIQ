"""
preprocessor.py - Feature Engineering Pipeline for Sales Forecasting

Academic Concepts:
- Time-series lag feature extraction (capturing autocorrelation at t-1, t-7, t-14).
- Rolling statistical features (moving average and moving volatility).
- Calendar/Seasonal indicators (day of week and weekend effects).
"""

import numpy as np
import pandas as pd


class DataPreprocessor:
    """Preprocesses time-series daily sales into supervised tabular features."""

    FEATURE_COLUMNS = [
        "lag_1",
        "lag_7",
        "lag_14",
        "rolling_mean_7",
        "rolling_std_7",
        "day_of_week",
        "is_weekend",
    ]

    @staticmethod
    def create_features(df_sales):
        """
        Transforms a DataFrame of [date, quantity] into feature-engineered tabular data.

        Features generated:
        1. lag_1: Sales quantity from previous day (t-1).
        2. lag_7: Sales quantity from same day last week (t-7).
        3. lag_14: Sales quantity from two weeks ago (t-14).
        4. rolling_mean_7: Moving average over the past 7 days.
        5. rolling_std_7: Standard deviation over the past 7 days (demand volatility).
        6. day_of_week: 0 (Monday) through 6 (Sunday).
        7. is_weekend: 1 for Saturday/Sunday, 0 for weekdays.

        Parameters:
            df_sales (pd.DataFrame): Columns ['date', 'quantity'].

        Returns:
            pd.DataFrame: Feature matrix X and target y with NaNs dropped.
        """
        df = df_sales.copy()
        df["date"] = pd.to_datetime(df["date"])
        df = df.sort_values("date").reset_index(drop=True)

        # Autoregressive Lag Features
        df["lag_1"] = df["quantity"].shift(1)
        df["lag_7"] = df["quantity"].shift(7)
        df["lag_14"] = df["quantity"].shift(14)

        # Rolling Statistics (Shifted by 1 to prevent data leakage from target)
        df["rolling_mean_7"] = (
            df["quantity"].shift(1).rolling(window=7, min_periods=1).mean()
        )
        df["rolling_std_7"] = (
            df["quantity"]
            .shift(1)
            .rolling(window=7, min_periods=1)
            .std()
            .fillna(0.0)
        )

        # Calendar Temporal Features
        df["day_of_week"] = df["date"].dt.dayofweek
        df["is_weekend"] = df["day_of_week"].apply(lambda d: 1 if d in [5, 6] else 0)

        # Drop the initial lag rows containing NaNs
        clean_df = df.dropna().reset_index(drop=True)
        return clean_df

    @classmethod
    def extract_X_y(cls, df_features):
        """Splits the feature-engineered DataFrame into X (features) and y (target)."""
        X = df_features[cls.FEATURE_COLUMNS]
        y = df_features["quantity"]
        return X, y
