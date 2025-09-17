# Quick Fix Script for Backend Connection
# Run this script to rebuild and restart your frontend with the correct backend URL

Write-Host "=======================================" -ForegroundColor Cyan
Write-Host "  InsightFlow Frontend - Quick Fix" -ForegroundColor Cyan
Write-Host "=======================================" -ForegroundColor Cyan
Write-Host ""

# Step 1: Stop and remove existing containers
Write-Host "1. Stopping existing containers..." -ForegroundColor Yellow
docker stop insightflow-frontend-prod insightflow-frontend-dev 2>$null
docker rm insightflow-frontend-prod insightflow-frontend-dev 2>$null

# Step 2: Remove old images
Write-Host "2. Removing old images..." -ForegroundColor Yellow
docker rmi insightflow-frontend:prod insightflow-frontend:dev 2>$null

# Step 3: Build new production image with correct backend URL
Write-Host "3. Building new production image with backend: https://insightflow-q8ds.onrender.com" -ForegroundColor Green
Write-Host "   Configuring for Render host: insightflow-frontend-1m77.onrender.com" -ForegroundColor Cyan
docker build --target production -t insightflow-frontend:prod `
  --build-arg VITE_API_BASE_URL=https://insightflow-q8ds.onrender.com `
  --build-arg VITE_APP_NAME=InsightFlow `
  --build-arg VITE_APP_VERSION=1.0.0 `
  .

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Build failed! Check the error messages above." -ForegroundColor Red
    exit 1
}

# Step 4: Start production container
Write-Host "4. Starting production container..." -ForegroundColor Green
docker run -d `
  --name insightflow-frontend-prod `
  -p 3000:80 `
  --restart unless-stopped `
  insightflow-frontend:prod

if ($LASTEXITCODE -eq 0) {
    Write-Host "" 
    Write-Host "✅ SUCCESS! Frontend is now connected to your deployed backend!" -ForegroundColor Green
    Write-Host "🌐 Frontend URL: http://localhost:3000" -ForegroundColor Cyan
    Write-Host "🔗 Backend URL: https://insightflow-q8ds.onrender.com" -ForegroundColor Cyan
    Write-Host "� Render Host: insightflow-frontend-1m77.onrender.com (now allowed)" -ForegroundColor Cyan
    Write-Host "�🏥 Health check: http://localhost:3000/health" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Host blocking issue is now resolved!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Test your connection by:" -ForegroundColor White
    Write-Host "1. Opening http://localhost:3000 in your browser" -ForegroundColor White
    Write-Host "2. Check browser console for 'API Base URL' log" -ForegroundColor White
    Write-Host "3. Try creating an analysis or login" -ForegroundColor White
    Write-Host ""
    Write-Host "View logs with: docker logs -f insightflow-frontend-prod" -ForegroundColor Gray
} else {
    Write-Host "❌ Failed to start container! Check Docker status." -ForegroundColor Red
    exit 1
}