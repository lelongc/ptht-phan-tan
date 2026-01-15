#!/bin/bash

# Docker Hub push script
# Usage: ./push-docker.sh <tag> (default: v1.0)

TAG="${1:-v1.0}"
IMAGE="shima594/ebook"

echo "📦 Building Docker image with tag: $TAG"
docker build -t "${IMAGE}:${TAG}" -t "${IMAGE}:latest" .

if [ $? -eq 0 ]; then
    echo "✅ Build successful!"
    echo ""
    echo "🔐 Login to Docker Hub..."
    docker login -u shima594
    
    if [ $? -eq 0 ]; then
        echo ""
        echo "📤 Pushing to Docker Hub..."
        docker push "${IMAGE}:${TAG}"
        docker push "${IMAGE}:latest"
        
        if [ $? -eq 0 ]; then
            echo ""
            echo "✅ Push successful!"
            echo "Image: https://hub.docker.com/r/shima594/ebook"
            echo "Tags: $TAG, latest"
        else
            echo "❌ Push failed!"
            exit 1
        fi
    else
        echo "❌ Docker login failed!"
        exit 1
    fi
else
    echo "❌ Build failed!"
    exit 1
fi
