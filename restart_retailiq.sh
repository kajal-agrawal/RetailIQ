#!/bin/bash
# ============================================================
# RetailIQ - Restart Utility Script
# ============================================================

set -e

PROJECT_DIR="/Users/kajalagrawal/.gemini/antigravity/scratch/RetailIQ"
cd "$PROJECT_DIR"

echo "Restarting RetailIQ..."
./stop_retailiq.sh
sleep 1
./start_retailiq.sh
