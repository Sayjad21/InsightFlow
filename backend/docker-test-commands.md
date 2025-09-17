# Backend Docker Testing Commands

## 1. Build the Docker Image

```powershell
# Navigate to backend directory
cd g:\InsightFlow\backend

# Build the image (this will run both build and runtime stages)
docker build -t insightflow-backend:latest .

# Alternative: Build with no cache (if you want fresh build)
docker build --no-cache -t insightflow-backend:latest .
```

## 2. Test the Container Locally

### Basic Run (using your MongoDB Atlas and external services)

```powershell
docker run -d `
  --name insightflow-backend-test `
  -p 8000:8000 `
  insightflow-backend:latest
```

### Run with Environment Override (if you want to test with different configs)

```powershell
docker run -d `
  --name insightflow-backend-test `
  -p 8000:8000 `
  -e SPRING_DATA_MONGODB_URI="mongodb+srv://insightflowuser:12345@cluster0.nxdt2fz.mongodb.net/insightflow?retryWrites=true&w=majority&appName=Cluster0" `
  -e SERVER_PORT=8000 `
  -e OLLAMA_BASE_URL="http://host.docker.internal:11434" `
  insightflow-backend:latest
```

### Run with Volume Mount (for debugging and file access)

```powershell
docker run -d `
  --name insightflow-backend-test `
  -p 8000:8000 `
  -v ${PWD}/uploaded_files:/app/uploaded_files `
  insightflow-backend:latest
```

## 3. Testing Commands

### Check if container is running

```powershell
docker ps
```

### View container logs

```powershell
docker logs insightflow-backend-test

# Follow logs in real-time
docker logs -f insightflow-backend-test
```

### Test health endpoint

```powershell
# Basic health check
curl http://localhost:8000/actuator/health

# Test a simple API endpoint
curl http://localhost:8000/api/sentiment/health
```

### Execute commands inside the container (for debugging)

```powershell
# Access container shell
docker exec -it insightflow-backend-test /bin/sh

# Check Java version inside container
docker exec insightflow-backend-test java -version

# Check installed packages
docker exec insightflow-backend-test apk list --installed
```

## 4. Cleanup Commands

### Stop and remove container

```powershell
docker stop insightflow-backend-test
docker rm insightflow-backend-test
```

### Remove image

```powershell
docker rmi insightflow-backend:latest
```

### Clean up all containers and images (use with caution)

```powershell
# Stop all containers
docker stop $(docker ps -aq)

# Remove all containers
docker rm $(docker ps -aq)

# Remove unused images
docker image prune -f
```

## 5. Troubleshooting Commands

### Check container resource usage

```powershell
docker stats insightflow-backend-test
```

### Inspect container configuration

```powershell
docker inspect insightflow-backend-test
```

### View container file system

```powershell
docker exec insightflow-backend-test ls -la /app
docker exec insightflow-backend-test ls -la /opt/openjdk-17/lib
```

## 6. Production-like Testing

### Test with Docker Compose (if you have MongoDB locally)

Create a `docker-compose.test.yml`:

```yaml
version: "3.8"
services:
  backend:
    build: .
    ports:
      - "8000:8000"
    environment:
      - SPRING_DATA_MONGODB_URI=mongodb://mongo:27017/insightflow
    depends_on:
      - mongo

  mongo:
    image: mongo:latest
    ports:
      - "27017:27017"
    volumes:
      - mongodb_data:/data/db

volumes:
  mongodb_data:
```

Run with:

```powershell
docker-compose -f docker-compose.test.yml up --build
```
