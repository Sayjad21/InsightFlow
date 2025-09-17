# InsightFlow Frontend Docker Setup

This document provides comprehensive instructions for containerizing and running the InsightFlow frontend application using Docker.

## Quick Start

### Production Build & Run

```powershell
# Build and run production container
docker build -t insightflow-frontend:latest .
docker run -p 3000:80 --name insightflow-frontend insightflow-frontend:latest
```

### Development Build & Run

```powershell
# Build and run development container with hot reload
docker build --target development -t insightflow-frontend:dev .
docker run -p 5173:5173 -v ${PWD}:/app -v /app/node_modules --name insightflow-frontend-dev insightflow-frontend:dev
```

## Detailed Instructions

### 1. Prerequisites

- Docker Desktop installed on Windows
- PowerShell or Command Prompt
- Port 3000 (production) or 5173 (development) available

### 2. Building Images

#### Production Image (Optimized with Nginx)

```powershell
# Navigate to frontend directory
cd "g:\InsightFlow\frontend"

# Build production image
docker build -t insightflow-frontend:prod .

# Alternative with build args
docker build --target production -t insightflow-frontend:prod .
```

#### Development Image (Hot Reload Support)

```powershell
# Build development image
docker build --target development -t insightflow-frontend:dev .
```

### 3. Running Containers

#### Production Container

```powershell
# Run production container
docker run -d `
  --name insightflow-frontend-prod `
  -p 3000:80 `
  --restart unless-stopped `
  insightflow-frontend:prod

# Check if running
docker ps

# View logs
docker logs insightflow-frontend-prod

# Access application at: http://localhost:3000
```

#### Development Container (with Volume Mounting)

```powershell
# Run development container with hot reload
docker run -d `
  --name insightflow-frontend-dev `
  -p 5173:5173 `
  -v "${PWD}:/app" `
  -v /app/node_modules `
  insightflow-frontend:dev

# Access development server at: http://localhost:5173
```

### 4. Container Management

#### Start/Stop Containers

```powershell
# Start container
docker start insightflow-frontend-prod

# Stop container
docker stop insightflow-frontend-prod

# Restart container
docker restart insightflow-frontend-prod

# Remove container
docker rm insightflow-frontend-prod
```

#### View Logs and Debug

```powershell
# View logs
docker logs insightflow-frontend-prod

# Follow logs (live)
docker logs -f insightflow-frontend-prod

# Execute commands in running container
docker exec -it insightflow-frontend-prod sh

# Check container health
docker inspect --format='{{.State.Health.Status}}' insightflow-frontend-prod
```

### 5. Image Management

#### List and Clean Images

```powershell
# List all images
docker images

# Remove image
docker rmi insightflow-frontend:prod

# Clean up unused images
docker image prune

# Remove all unused containers, networks, images
docker system prune -a
```

### 6. Environment Configuration

#### Production Environment Variables (Optional)

```powershell
# Run with environment variables
docker run -d `
  --name insightflow-frontend-prod `
  -p 3000:80 `
  -e NODE_ENV=production `
  -e VITE_API_BASE_URL=http://localhost:8080 `
  insightflow-frontend:prod
```

#### Development with .env File

```powershell
# Run development container with .env file
docker run -d `
  --name insightflow-frontend-dev `
  -p 5173:5173 `
  -v "${PWD}:/app" `
  -v /app/node_modules `
  --env-file .env `
  insightflow-frontend:dev
```

### 7. Performance Optimization

#### Build with Cache Optimization

```powershell
# Build with build cache
docker build --cache-from insightflow-frontend:latest -t insightflow-frontend:latest .

# Build with no cache (clean build)
docker build --no-cache -t insightflow-frontend:latest .
```

### 8. Health Checks and Monitoring

#### Health Check Commands

```powershell
# Manual health check
curl http://localhost:3000/health

# Check container health status
docker inspect --format='{{.State.Health.Status}}' insightflow-frontend-prod
```

### 9. Troubleshooting

#### Common Issues and Solutions

**Port Already in Use:**

```powershell
# Find process using port
netstat -ano | findstr :3000

# Kill process (replace PID with actual process ID)
taskkill /PID <PID> /F

# Or use different port
docker run -p 3001:80 insightflow-frontend:prod
```

**Container Won't Start:**

```powershell
# Check container logs
docker logs insightflow-frontend-prod

# Check container status
docker inspect insightflow-frontend-prod
```

**Build Failures:**

```powershell
# Clean build without cache
docker build --no-cache -t insightflow-frontend:latest .

# Check Docker daemon
docker info

# Clean up Docker system
docker system prune -f
```

### 10. Integration with Backend

#### Network Setup for Full Stack

```powershell
# Create custom network
docker network create insightflow-network

# Run frontend with custom network
docker run -d `
  --name insightflow-frontend-prod `
  --network insightflow-network `
  -p 3000:80 `
  insightflow-frontend:prod

# Run backend on same network (example)
docker run -d `
  --name insightflow-backend `
  --network insightflow-network `
  -p 8080:8080 `
  insightflow-backend:latest
```

## File Structure Created

```
frontend/
├── Dockerfile              # Multi-stage Docker build file
├── .dockerignore           # Files/folders to exclude from build context
├── nginx.conf              # Nginx configuration for production
└── DOCKER_INSTRUCTIONS.md  # This documentation file
```

## Container Architecture

- **Base Image**: Node 22 Alpine (lightweight)
- **Production Server**: Nginx Alpine
- **Build Strategy**: Multi-stage build for minimal production image
- **Security**: Non-root user, security headers
- **Performance**: Gzip compression, static asset caching
- **Health Checks**: Built-in health monitoring

## Ports

- **Development**: 5173 (Vite dev server)
- **Production**: 80 (Nginx, mapped to host port 3000)

Your frontend is now fully containerized with optimized production and development configurations!
