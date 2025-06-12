# 🚀 Deployment HTTPS para Germogli Backend

## 📋 Configuración Completada

Este proyecto ahora soporta completamente **HTTP y HTTPS** con redirección automática.

### ✅ Características Implementadas:

- **HTTPS nativo** en puerto 8443 con certificado SSL
- **HTTP** en puerto 8080 con redirección automática a HTTPS
- **Headers de seguridad** (HSTS, X-Frame-Options, etc.)
- **Perfiles Spring Boot** (default/production)
- **Deployment automatizado** en Azure Container Instances

## 🌐 URLs Disponibles:

### Producción (Azure):
- **HTTPS:** `https://germogli-backend.eastus.azurecontainer.io:8443`
- **HTTP:** `http://germogli-backend.eastus.azurecontainer.io:8080` (redirige a HTTPS)

### Endpoints principales:
- **Login:** `POST /auth/login`
- **Health:** `GET /actuator/health`
- **API Base:** `https://germogli-backend.eastus.azurecontainer.io:8443/api/`

## 🛠️ Comandos de Administración:

### Encender Backend:
```bash
az container create --resource-group germogli-rg --file container-group-complete.yaml
```

### Apagar Backend:
```bash
az container delete --name germogli-backend-container --resource-group germogli-rg --yes
```

### Ver Estado:
```bash
az container show --name germogli-backend-container --resource-group germogli-rg --output table
```

### Ver Logs:
```bash
az container logs --name germogli-backend-container --resource-group germogli-rg
```

## 📁 Archivos de Configuración:

### Spring Boot:
- `src/main/resources/application.properties` - Configuración base HTTP
- `src/main/resources/application-production.properties` - Configuración HTTPS
- `src/main/java/com/germogli/backend/common/config/HttpsConfig.java` - Redirección HTTP→HTTPS

### SSL/TLS:
- `src/main/resources/ssl/keystore.p12` - Certificado SSL (autofirmado)

### Azure Deployment:
- `container-group-template.yaml` - Template para despliegue (sin secretos)
- `container-group-complete.local.yaml` - Archivo real con secrets (gitignored)
- `container-group-https.local.yaml` - Archivo HTTPS con secrets (gitignored)

## 🔧 Setup Local para Desarrollo:

### 1. Copiar template:
```bash
cp container-group-template.yaml container-group-complete.yaml
```

### 2. Completar secretos en `container-group-complete.yaml`:
- `TU_DB_PASSWORD_AQUI` → Password de base de datos
- `TU_AZURE_CONNECTION_STRING_AQUI` → Connection string de Azure Storage
- `TU_JWT_SECRET_AQUI` → Secret key para JWT
- `TU_REGISTRY_PASSWORD_AQUI` → Password de Azure Container Registry

### 3. Variables de entorno (.env ya configurado):
- ✅ DB_URL
- ✅ DB_USERNAME 
- ✅ DB_PASSWORD
- ✅ AZURE_CONNECTION_STRING
- ✅ JWT_SECRET
- ✅ COOKIE_NAME

## 🧪 Testing:

### Probar HTTPS:
```bash
curl "https://germogli-backend.eastus.azurecontainer.io:8443/auth/login" \
  -X POST \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin"}' \
  -k
```

### Probar redirección HTTP→HTTPS:
```bash
curl "http://germogli-backend.eastus.azurecontainer.io:8080/auth/login" \
  -X POST \
  -I
# Debe devolver: 302 Location: https://...:8443/auth/login
```

## 🔒 Seguridad:

### Headers implementados:
- `Strict-Transport-Security` - Fuerza HTTPS
- `X-Frame-Options: DENY` - Previene clickjacking
- `X-Content-Type-Options: nosniff` - Previene MIME sniffing
- `X-XSS-Protection` - Protección XSS básica

### Certificado SSL:
- **Tipo:** Autofirmado (para desarrollo)
- **Algoritmo:** RSA 2048 bits
- **Validez:** 365 días
- **CN:** germogli-backend.eastus.azurecontainer.io

⚠️ **Para producción:** Reemplazar con certificado válido de CA

## 📊 Monitoreo:

### Estado del contenedor:
```bash
Test-NetConnection -ComputerName germogli-backend.eastus.azurecontainer.io -Port 8443
```

### Obtener IP actual:
```bash
az container show --name germogli-backend-container --resource-group germogli-rg --query "ipAddress.ip" --output tsv
```

---

## 🎯 Próximos Pasos:

1. **Certificado válido:** Implementar Let's Encrypt o certificado comercial
2. **Load Balancer:** Azure Application Gateway para terminación SSL
3. **Dominio personalizado:** Configurar dominio propio
4. **CI/CD:** Pipeline automatizado para deployment
5. **Monitoring:** Azure Monitor + Application Insights

---

💡 **Nota:** Los archivos con secretos están en `.gitignore` por seguridad.

