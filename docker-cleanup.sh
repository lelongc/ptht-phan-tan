#!/bin/bash

# Docker cleanup script - remove unused resources
# Usage: ./docker-cleanup.sh

echo "🧹 Starting Docker cleanup..."
echo ""

# Remove dangling images
echo "🗑️  Removing dangling images..."
docker image prune -f

# Remove unused volumes
echo "🗑️  Removing unused volumes..."
docker volume prune -f

# Remove unused networks
echo "🗑️  Removing unused networks..."
docker network prune -f

# Optional: Remove stopped containers
echo "🗑️  Removing stopped containers..."
docker container prune -f

echo ""
echo "📊 Docker cleanup complete!"
echo ""
echo "Disk usage:"
du -sh /var/lib/docker 2>/dev/null || echo "N/A"

echo ""
echo "Active containers:"
docker ps --format "table {{.Names}}\t{{.Status}}"

echo ""
echo "Active images:"
docker images --format "table {{.Repository}}:{{.Tag}}\t{{.Size}}"
