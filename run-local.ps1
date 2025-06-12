# Script para ejecutar la aplicación con configuración local
# Carga las variables del archivo .env.local automáticamente

Write-Host "=== Germogli Backend - Ejecución Local ==="
Write-Host ""

# Verificar si existe .env.local
if (-not (Test-Path ".env.local")) {
    Write-Warning "No se encontró el archivo .env.local"
    Write-Host "Usando configuración por defecto (root/root)"
} else {
    Write-Host "Cargando variables de .env.local..."
    
    # Leer y cargar variables del archivo .env.local
    Get-Content ".env.local" | ForEach-Object {
        if ($_ -match '^([^=]+)=(.*)$') {
            $name = $matches[1].Trim()
            $value = $matches[2].Trim()
            
            # Solo cargar si no es un comentario
            if (-not $name.StartsWith('#')) {
                [Environment]::SetEnvironmentVariable($name, $value, 'Process')
                Write-Host "  $name configurado"
            }
        }
    }
}

Write-Host ""
Write-Host "Ejecutando aplicación..."
Write-Host "URL: http://localhost:8080"
Write-Host "Presiona Ctrl+C para detener"
Write-Host ""

# Ejecutar la aplicación
mvn spring-boot:run "-Dspring-boot.run.profiles=local"

