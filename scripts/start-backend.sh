#!/bin/bash

echo "🔧 Starting Backend Services..."

cd ../infra

# Start only backend dependencies and backend
docker-compose up --build -d postgres mosquitto backend

echo "✅ Backend services started!"
echo "📡 Backend API: http://localhost:8080/api"
echo "🗄️  PostgreSQL: localhost:5432"
echo "📨 MQTT Broker: localhost:1883"