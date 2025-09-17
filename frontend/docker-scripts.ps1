# InsightFlow Frontend Docker Management Scripts
# PowerShell scripts for easy container management

# Build production image
function Build-Production {
    Write-Host "Building InsightFlow Frontend (Production)..." -ForegroundColor Green
    docker build --target production -t insightflow-frontend:prod `
      --build-arg VITE_API_BASE_URL=https://insightflow-q8ds.onrender.com `
      --build-arg VITE_APP_NAME=InsightFlow `
      --build-arg VITE_APP_VERSION=1.0.0 `
      .
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Production build completed successfully!" -ForegroundColor Green
        Write-Host "🌐 API Base URL: https://insightflow-q8ds.onrender.com" -ForegroundColor Cyan
    } else {
        Write-Host "❌ Production build failed!" -ForegroundColor Red
    }
}

# Build development image
function Build-Development {
    Write-Host "Building InsightFlow Frontend (Development)..." -ForegroundColor Green
    docker build --target development -t insightflow-frontend:dev `
      --build-arg VITE_API_BASE_URL=http://localhost:8000 `
      --build-arg VITE_APP_NAME=InsightFlow `
      --build-arg VITE_APP_VERSION=1.0.0 `
      --build-arg VITE_DEBUG=true `
      .
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Development build completed successfully!" -ForegroundColor Green
        Write-Host "🌐 API Base URL: http://localhost:8000" -ForegroundColor Cyan
    } else {
        Write-Host "❌ Development build failed!" -ForegroundColor Red
    }
}

# Run production container
function Start-Production {
    Write-Host "Starting InsightFlow Frontend (Production)..." -ForegroundColor Green
    
    # Stop and remove existing container if it exists
    docker stop insightflow-frontend-prod 2>$null
    docker rm insightflow-frontend-prod 2>$null
    
    # Start new container
    docker run -d `
      --name insightflow-frontend-prod `
      -p 3000:80 `
      --restart unless-stopped `
      insightflow-frontend:prod
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Production container started successfully!" -ForegroundColor Green
        Write-Host "🌐 Access your application at: http://localhost:3000" -ForegroundColor Cyan
        Write-Host "🏥 Health check at: http://localhost:3000/health" -ForegroundColor Cyan
    } else {
        Write-Host "❌ Failed to start production container!" -ForegroundColor Red
    }
}

# Run development container
function Start-Development {
    Write-Host "Starting InsightFlow Frontend (Development)..." -ForegroundColor Green
    
    # Stop and remove existing container if it exists
    docker stop insightflow-frontend-dev 2>$null
    docker rm insightflow-frontend-dev 2>$null
    
    # Start new container with volume mounting
    docker run -d `
      --name insightflow-frontend-dev `
      -p 5173:5173 `
      -v "${PWD}:/app" `
      -v /app/node_modules `
      insightflow-frontend:dev
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Development container started successfully!" -ForegroundColor Green
        Write-Host "🌐 Access your development server at: http://localhost:5173" -ForegroundColor Cyan
        Write-Host "🔥 Hot reload is enabled - changes will reflect automatically!" -ForegroundColor Yellow
    } else {
        Write-Host "❌ Failed to start development container!" -ForegroundColor Red
    }
}

# Stop all containers
function Stop-All {
    Write-Host "Stopping all InsightFlow Frontend containers..." -ForegroundColor Yellow
    docker stop insightflow-frontend-prod insightflow-frontend-dev 2>$null
    Write-Host "✅ All containers stopped!" -ForegroundColor Green
}

# View logs
function Show-Logs {
    param(
        [Parameter(Mandatory=$false)]
        [ValidateSet("prod", "dev")]
        [string]$Environment = "prod"
    )
    
    $containerName = if ($Environment -eq "prod") { "insightflow-frontend-prod" } else { "insightflow-frontend-dev" }
    Write-Host "Showing logs for $containerName..." -ForegroundColor Cyan
    docker logs -f $containerName
}

# Clean up containers and images
function Clean-All {
    Write-Host "Cleaning up InsightFlow Frontend containers and images..." -ForegroundColor Yellow
    
    # Stop and remove containers
    docker stop insightflow-frontend-prod insightflow-frontend-dev 2>$null
    docker rm insightflow-frontend-prod insightflow-frontend-dev 2>$null
    
    # Remove images
    docker rmi insightflow-frontend:prod insightflow-frontend:dev 2>$null
    
    Write-Host "✅ Cleanup completed!" -ForegroundColor Green
}

# Quick status check
function Get-Status {
    Write-Host "InsightFlow Frontend Container Status:" -ForegroundColor Cyan
    Write-Host "=======================================" -ForegroundColor Cyan
    
    $prodStatus = docker ps -a --filter name=insightflow-frontend-prod --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
    $devStatus = docker ps -a --filter name=insightflow-frontend-dev --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
    
    if ($prodStatus) {
        Write-Host "Production Container:" -ForegroundColor Green
        Write-Host $prodStatus
    } else {
        Write-Host "Production Container: Not found" -ForegroundColor Red
    }
    
    if ($devStatus) {
        Write-Host "Development Container:" -ForegroundColor Green
        Write-Host $devStatus
    } else {
        Write-Host "Development Container: Not found" -ForegroundColor Red
    }
}

# Main menu function
function Show-Menu {
    Clear-Host
    Write-Host "===============================================" -ForegroundColor Cyan
    Write-Host "    InsightFlow Frontend Docker Manager" -ForegroundColor Cyan
    Write-Host "===============================================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "1. Build Production Image" -ForegroundColor White
    Write-Host "2. Build Development Image" -ForegroundColor White
    Write-Host "3. Start Production Container" -ForegroundColor Green
    Write-Host "4. Start Development Container" -ForegroundColor Green
    Write-Host "5. Stop All Containers" -ForegroundColor Yellow
    Write-Host "6. Show Logs (Production)" -ForegroundColor Cyan
    Write-Host "7. Show Logs (Development)" -ForegroundColor Cyan
    Write-Host "8. Container Status" -ForegroundColor Magenta
    Write-Host "9. Clean All (Remove containers & images)" -ForegroundColor Red
    Write-Host "0. Exit" -ForegroundColor Gray
    Write-Host ""
}

# Interactive menu
function Start-Menu {
    do {
        Show-Menu
        $choice = Read-Host "Select an option (0-9)"
        
        switch ($choice) {
            "1" { Build-Production; Pause }
            "2" { Build-Development; Pause }
            "3" { Start-Production; Pause }
            "4" { Start-Development; Pause }
            "5" { Stop-All; Pause }
            "6" { Show-Logs -Environment "prod" }
            "7" { Show-Logs -Environment "dev" }
            "8" { Get-Status; Pause }
            "9" { 
                $confirm = Read-Host "Are you sure you want to clean all? (y/N)"
                if ($confirm -eq "y" -or $confirm -eq "Y") {
                    Clean-All
                }
                Pause
            }
            "0" { Write-Host "Goodbye!" -ForegroundColor Green; break }
            default { Write-Host "Invalid option. Please try again." -ForegroundColor Red; Pause }
        }
    } while ($choice -ne "0")
}

function Pause {
    Write-Host ""
    Write-Host "Press any key to continue..." -ForegroundColor Gray
    $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
}

# Export functions for direct use
Export-ModuleMember -Function *

# If script is run directly, show the menu
if ($MyInvocation.InvocationName -eq $MyInvocation.MyCommand.Name) {
    Start-Menu
}