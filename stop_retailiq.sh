#!/bin/bash
# ============================================================
# RetailIQ - Clean Shutdown Utility Script
# ============================================================

PROJECT_DIR="/Users/kajalagrawal/.gemini/antigravity/scratch/RetailIQ"
PID_FILE="$PROJECT_DIR/.ml_service.pid"

echo "Stopping RetailIQ background services..."

# Stop Flask if running from PID file
if [ -f "$PID_FILE" ]; then
    PID=$(cat "$PID_FILE")
    if kill -0 "$PID" 2>/dev/null; then
        echo "Killing Python ML Microservice (PID: $PID)..."
        kill -9 "$PID" 2>/dev/null || true
    fi
    rm -f "$PID_FILE"
fi

# Force kill any processes listening on ports 5001 or 8080
PID_5001=$(lsof -t -i:5001 2>/dev/null || true)
if [ -n "$PID_5001" ]; then
    echo "Killing process on port 5001 ($PID_5001)..."
    kill -9 $PID_5001 2>/dev/null || true
fi

PID_8080=$(lsof -t -i:8080 2>/dev/null || true)
if [ -n "$PID_8080" ]; then
    echo "Killing process on port 8080 ($PID_8080)..."
    kill -9 $PID_8080 2>/dev/null || true
fi

echo "RetailIQ ports (5001, 8080) are clear and released."
