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

# Kill any stray processes listening on ports 5001 or 8080
lsof -ti:5001 | xargs kill -9 2>/dev/null || true
lsof -ti:8080 | xargs kill -9 2>/dev/null || true

echo "RetailIQ ports (5001, 8080) are clear and released."
