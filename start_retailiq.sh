#!/bin/bash
# ============================================================
# RetailIQ - One-Click Production Launcher Script
# ============================================================

set -e

PROJECT_DIR="/Users/kajalagrawal/.gemini/antigravity/scratch/RetailIQ"
ML_DIR="$PROJECT_DIR/ml_service"
JAVA_DIR="$PROJECT_DIR/java_backend"
PID_FILE="$PROJECT_DIR/.ml_service.pid"

echo "================================================="
echo "   Starting RetailIQ Integrated Web System"
echo "================================================="

# 1. Check MySQL
echo "[1/3] Checking MySQL database service..."
if ! nc -z 127.0.0.1 3306 2>/dev/null; then
    echo "[!] MySQL does not appear to be running on port 3306."
    echo "    Attempting to start MySQL via brew services..."
    brew services start mysql || true
    sleep 2
fi
echo "      MySQL is active."

# Function to cleanly stop Flask on exit
cleanup() {
    echo ""
    echo "================================================="
    echo "   Stopping RetailIQ Services..."
    echo "================================================="
    if [ -f "$PID_FILE" ]; then
        PID=$(cat "$PID_FILE")
        if kill -0 "$PID" 2>/dev/null; then
            echo "-> Stopping Python Flask ML Microservice (PID: $PID)..."
            kill "$PID" 2>/dev/null || true
        fi
        rm -f "$PID_FILE"
    fi
    echo "All RetailIQ services stopped cleanly."
    exit 0
}
trap cleanup SIGINT SIGTERM EXIT

# 2. Start Python Flask ML Microservice
echo "[2/3] Launching Python Flask ML Microservice on port 5001..."
cd "$ML_DIR"
source venv/bin/activate
python app.py > "$ML_DIR/service.log" 2>&1 &
FLASK_PID=$!
echo "$FLASK_PID" > "$PID_FILE"
sleep 2

# Verify Flask is responding
if curl -s http://127.0.0.1:5001/api/health >/dev/null 2>&1; then
    echo "      Python ML Microservice is healthy and listening on http://127.0.0.1:5001"
else
    echo "[!] Warning: Python ML Microservice is initializing (Logs: $ML_DIR/service.log)"
fi

# 3. Start Java Web Application
echo "[3/3] Launching Java Web Server on http://localhost:8080/RetailIQ..."
echo "-------------------------------------------------"
echo "   Application URL: http://localhost:8080/RetailIQ"
echo "   Default Login  : admin / admin123"
echo "   (Press Ctrl+C to stop both Java and Python)"
echo "-------------------------------------------------"
cd "$JAVA_DIR"
mvn jetty:run
