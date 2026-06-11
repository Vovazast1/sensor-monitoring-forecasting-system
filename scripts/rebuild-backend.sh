#!/bin/bash
set -e

echo "🔨 Building backend..."
cd "$(dirname "$0")/.."

./gradlew bootJar -q

echo "🐳 Rebuilding container..."
cd infra
docker-compose up --build -d backend

echo "✅ Done! Backend restarted."
