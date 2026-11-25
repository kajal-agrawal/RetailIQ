"""
test_flask_api.py - Automated PyTest suite for Flask REST API Endpoints
"""

from datetime import datetime, timedelta
import json
import os
import sys
import pytest

sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))
from app import app


@pytest.fixture
def client():
    app.config["TESTING"] = True
    with app.test_client() as client:
        yield client


def test_healthcheck_endpoint(client):
    res = client.get("/api/health")
    assert res.status_code == 200
    data = json.loads(res.data)
    assert data["status"] == "healthy"


def test_stats_endpoint(client):
    payload = {"sales": [10, 15, 8, 20, 12, 14, 18], "lead_time_days": 3}
    res = client.post(
        "/api/stats", data=json.dumps(payload), content_type="application/json"
    )
    assert res.status_code == 200
    data = json.loads(res.data)
    assert data["status"] == "success"
    assert "descriptive_statistics" in data
    assert "safety_stock_parameters" in data


def test_forecast_endpoint(client):
    history = []
    base_date = datetime(2026, 1, 1)
    for i in range(40):
        d = base_date + timedelta(days=i)
        history.append({"date": d.strftime("%Y-%m-%d"), "quantity": 10 + (i % 5)})

    payload = {
        "product_id": 1,
        "lead_time_days": 3,
        "horizon_days": 7,
        "history": history,
    }
    res = client.post(
        "/api/forecast",
        data=json.dumps(payload),
        content_type="application/json",
    )
    assert res.status_code == 200
    data = json.loads(res.data)
    assert data["status"] == "success"
    assert "models" in data
    assert "recommended_model" in data
    assert len(data["forecast_dates"]) == 7
