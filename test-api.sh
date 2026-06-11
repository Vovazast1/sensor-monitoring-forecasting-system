#!/bin/bash

# Quick Start Script for REST API Testing

echo "🚀 Sensors Monitoring Service - REST API Quick Start"
echo "=================================================="
echo ""

# Check if services are running
echo "📋 Checking services..."
echo ""

# Check PostgreSQL
if pg_isready -h localhost -p 5432 > /dev/null 2>&1; then
    echo "✅ PostgreSQL is running"
else
    echo "❌ PostgreSQL is not running. Start it with: brew services start postgresql"
fi

# Check ML Service
if curl -s http://localhost:8000/health > /dev/null 2>&1; then
    echo "✅ ML Service is running (port 8000)"
else
    echo "❌ ML Service is not running. Start it with: cd sensor-ml-service && python3 run.py"
fi

# Check Spring Boot
if curl -s http://localhost:8080/api/devices > /dev/null 2>&1; then
    echo "✅ Spring Boot is running (port 8080)"
else
    echo "❌ Spring Boot is not running. Start it with: ./gradlew bootRun"
fi

echo ""
echo "=================================================="
echo "📡 Testing REST API Endpoints"
echo "=================================================="
echo ""

# Test 1: Get all devices
echo "1️⃣  GET /api/devices"
curl -s http://localhost:8080/api/devices | jq '.' || echo "Failed"
echo ""

# Test 2: Get all sensors
echo "2️⃣  GET /api/sensors"
curl -s http://localhost:8080/api/sensors | jq '.' || echo "Failed"
echo ""

# Test 3: Get sensor telemetry
echo "3️⃣  GET /api/sensors/1/telemetry/latest"
curl -s http://localhost:8080/api/sensors/1/telemetry/latest | jq '.' || echo "Failed"
echo ""

# Test 4: ML Prediction for sensor
echo "4️⃣  GET /api/ml/predict/sensor/1"
curl -s "http://localhost:8080/api/ml/predict/sensor/1?threshold=30.0" | jq '.' || echo "Failed"
echo ""

echo "=================================================="
echo "✨ Quick Start Complete!"
echo "=================================================="
echo ""
echo "📚 Documentation:"
echo "   - REST_API.md - Full API documentation"
echo "   - REST_API_IMPLEMENTATION.md - Implementation details"
echo ""
echo "🔗 Useful URLs:"
echo "   - Frontend: http://localhost:3000"
echo "   - Backend API: http://localhost:8080/api"
echo "   - ML Service: http://localhost:8000"
echo "   - ML Swagger: http://localhost:8000/docs"
echo ""
