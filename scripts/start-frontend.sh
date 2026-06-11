#!/bin/bash

echo "🎨 Starting Frontend in Development Mode..."

# Stop Docker frontend if running
echo "🛑 Stopping Docker frontend container..."
docker stop sensors-frontend 2>/dev/null || true
docker rm sensors-frontend 2>/dev/null || true

# Kill existing React dev server
echo "🛑 Stopping existing React dev server..."
pkill -f "react-scripts/scripts/start.js" 2>/dev/null || true

cd ../frontend

# Install dependencies if needed
if [ ! -d "node_modules" ]; then
    echo "📦 Installing dependencies..."
    npm install
fi

# Start development server
echo "🚀 Starting React development server..."
npm start

echo "✅ Frontend started!"
echo "🌐 Frontend: http://localhost:3000"