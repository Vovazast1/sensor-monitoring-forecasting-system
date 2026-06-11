#!/bin/bash

echo "🚀 Starting Sensors Monitoring System..."

if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker first."
    exit 1
fi

echo "📦 Building and starting backend services..."
./start-backend.sh

echo "📦 Building and starting frontend services..."
./start-frontend.sh

echo "🔍 Checking service status..."
docker-compose ps

echo ""
echo "✅ System is starting up!"