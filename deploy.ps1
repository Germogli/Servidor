# Script de despliegue para Germogli Backend con Azure MySQL
# Ejecutar con: .\deploy.ps1

Write-Host "=== Iniciando despliegue de Germogli Backend ==="

# Verificar que existe el archivo .env
if (-not (Test-Path ".env")) {
    Write-Host "Error: No se encontro el archivo .env" -ForegroundColor Red
    Write-Host "Copia .env.example a .env y configura tus credenciales" -ForegroundColor Yellow
    exit 1
}

# Verificar que Docker esta corriendo
try {
    docker version | Out-Null
    Write-Host "✓ Docker esta corriendo" -ForegroundColor Green
} catch {
    Write-Host "Error: Docker no esta corriendo" -ForegroundColor Red
    exit 1
}

# Construir la imagen Docker
Write-Host "=== Construyendo imagen Docker ==="
docker build -t backend-app:latest .
if ($LASTEXITCODE -ne 0) {
    Write-Host "Error al construir la imagen Docker" -ForegroundColor Red
    exit 1
}
Write-Host "✓ Imagen Docker construida exitosamente" -ForegroundColor Green

# Detener contenedores previos si existen
Write-Host "=== Deteniendo contenedores previos ==="
docker-compose down

# Iniciar los servicios
Write-Host "=== Iniciando servicios ==="
docker-compose up -d
if ($LASTEXITCODE -ne 0) {
    Write-Host "Error al iniciar los servicios" -ForegroundColor Red
    exit 1
}

Write-Host "✓ Servicios iniciados exitosamente" -ForegroundColor Green
Write-Host "=== Despliegue completado ==="
Write-Host "Backend disponible en: http://localhost:8080" -ForegroundColor Cyan
Write-Host "Adminer disponible en: http://localhost:8081" -ForegroundColor Cyan
Write-Host "Para ver logs: docker-compose logs -f backend" -ForegroundColor Yellow

