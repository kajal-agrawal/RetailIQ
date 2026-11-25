# ============================================================
# Python ML Microservice Dockerfile
# ============================================================
FROM python:3.9-slim

WORKDIR /app

# Install system dependencies
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Copy and install python dependencies
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt gunicorn

# Copy application source
COPY . .

EXPOSE 5001

# Production WSGI server (Gunicorn with 2 workers)
CMD ["gunicorn", "--workers=2", "--bind=0.0.0.0:5001", "app:app"]
