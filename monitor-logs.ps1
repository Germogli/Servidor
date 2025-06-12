# Script para monitorear logs del backend en tiempo real
# Uso: .\monitor-logs.ps1

Write-Host "🔍 Monitoreando logs del backend en tiempo real..." -ForegroundColor Green
Write-Host "📡 URL: https://germogli-backend.eastus.azurecontainer.io:8443" -ForegroundColor Cyan
Write-Host "⏹️  Presiona Ctrl+C para detener" -ForegroundColor Yellow
Write-Host "" 

$lastLogTime = Get-Date

while ($true) {
    try {
        Write-Host "[$(Get-Date -Format 'HH:mm:ss')] Obteniendo logs..." -ForegroundColor DarkGray
        
        # Obtener logs del container
        $logs = az container logs --name germogli-backend-container --resource-group germogli-rg 2>$null
        
        if ($logs) {
            # Mostrar solo las últimas líneas (nuevas)
            $logLines = $logs -split "`n"
            $recentLines = $logLines | Select-Object -Last 10
            
            foreach ($line in $recentLines) {
                if ($line -match "INFO") {
                    Write-Host $line -ForegroundColor White
                } elseif ($line -match "WARN") {
                    Write-Host $line -ForegroundColor Yellow
                } elseif ($line -match "ERROR") {
                    Write-Host $line -ForegroundColor Red
                } elseif ($line -match "DEBUG") {
                    Write-Host $line -ForegroundColor DarkGray
                } else {
                    Write-Host $line -ForegroundColor Gray
                }
            }
        }
        
        Write-Host "" 
        Start-Sleep -Seconds 5
        
    } catch {
        Write-Host "❌ Error obteniendo logs: $_" -ForegroundColor Red
        Start-Sleep -Seconds 10
    }
}

