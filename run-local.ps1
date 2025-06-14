# Script para ejecutar la aplicacion con configuracion local
# Carga las variables del archivo .env.local automaticamente

Write-Host "=== Germogli Backend - Ejecucion Local ==="
Write-Host ""

# Verificar si existe .env.local
if (-not (Test-Path ".env.local")) {
    Write-Warning "No se encontro el archivo .env.local"
    Write-Host "La aplicacion puede fallar sin las variables de entorno necesarias"
    exit 1
}

Write-Host "Cargando variables de .env.local..."

# Leer variables del archivo .env.local
Get-Content ".env.local" | ForEach-Object {
    if ($_ -match '^([^=]+)=(.*)$') {
        $name = $matches[1].Trim()
        $value = $matches[2].Trim()
        
        # Solo cargar si no es un comentario
        if (-not $name.StartsWith('#') -and $name -ne '') {
            [Environment]::SetEnvironmentVariable($name, $value, 'Process')
            Write-Host "  $name configurado"
        }
    }
}

Write-Host ""
Write-Host "Ejecutando aplicacion..."
Write-Host "URL: http://localhost:8080"
Write-Host "Perfil: local"
Write-Host "Presiona Ctrl+C para detener"
Write-Host ""

# Ejecutar la aplicacion usando Maven Wrapper
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"

