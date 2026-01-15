# Docker Deployment Guide

## Quick Push to Docker Hub

### Method 1: Using Script (Recommended)
```bash
# Default tag v1.0
./push-docker.sh

# Or with custom tag
./push-docker.sh v1.1
```

### Method 2: Manual Commands
```bash
# Build with tags
docker build -t shima594/ebook:v1.0 -t shima594/ebook:latest .

# Login
docker login -u shima594

# Push both tags
docker push shima594/ebook:v1.0
docker push shima594/ebook:latest
```

## Cache Optimization

The Dockerfile uses **BuildKit layer caching**:

1. **Maven Dependencies Layer** (cached):
   - Only rebuilt if `pom.xml` changes
   - First build: ~51 seconds
   - Subsequent builds: 0.5 seconds ⚡

2. **Source Code Layer**:
   - Only rebuilt if `src/` changes
   - Compilation: ~7 seconds

3. **Runtime Layer**:
   - Always uses cached base JRE image

### Build Time Comparison:
- ❌ First build: **86 seconds**
- ✅ Rebuild with no changes: **0.9 seconds** (all cached!)
- ✅ Rebuild with src changes only: **8 seconds** (deps cached)

### Enable BuildKit (Optional)
```bash
export DOCKER_BUILDKIT=1
# or enable in Docker Desktop settings
```

## Image Details

- **Size**: 325 MB (optimized multi-stage build)
- **Base**: eclipse-temurin:17-jre (runtime only)
- **Registry**: Docker Hub (shima594/ebook)
- **Tags**: v1.0, latest

## Production Pull
```bash
docker pull shima594/ebook:v1.0
docker run -d -p 8080:8080 --env-file .env.local shima594/ebook:v1.0
```

## Push History

### v1.0 (2026-01-15)
- ✅ PayPal Sandbox integration
- ✅ Bilingual UI (VI: VND, EN: USD)
- ✅ Email duplicate payment check
- ✅ Docker BuildKit cache optimization
