#!/bin/bash

echo "🧹 Cleaning up Sensors Monitoring System..."

cd infra

docker-compose down --remove-orphans

# echo "🧹 Deleted volumes (Database)"
# docker-compose down -v

echo "✅ Cleanup complete!"