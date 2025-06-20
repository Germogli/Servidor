# 🚀 Deployment HTTPS para Germogli Backend

## 🎉 SSL VÁLIDO IMPLEMENTADO EXITOSAMENTE

**Estado:** ✅ **FUNCIONANDO** - Certificado SSL válido y confiable

### ✅ Características Implementadas:

- **SSL Válido:** Certificado Let's Encrypt (sin advertencias del navegador)
- **Application Gateway:** Terminación SSL profesional
- **Backend HTTP:** Optimizado sin SSL interno
- **Health Monitoring:** Monitoreo automático del estado
- **CORS Configurado:** Dominios SSL incluidos
- **Configuración Limpia:** Sin duplicaciones ni conflictos

## 🌐 URLs FINALES FUNCIONANDO

### ✅ URL Principal (SSL Válido):
```
https://germogli-app.eastus.cloudapp.azure.com
```

### ✅ URL HTTP (También disponible):
```
http://germogli-app.eastus.cloudapp.azure.com
```

### 🔗 Endpoints principales:
- **Login:** `POST /auth/login`
- **API Base:** `https://germogli-app.eastus.cloudapp.azure.com/api/`
- **WebSocket:** `wss://germogli-app.eastus.cloudapp.azure.com/ws`

## 🛠️ GESTIÓN DEL BACKEND (REDUCIR COSTOS)

### ⚡ ENCENDER Backend (Start):
```bash
az container start --name germogli-backend-container --resource-group germogli-rg
```
**Resultado:** ✅ Inicia el backend y se reanuda la facturación

### 🛑 APAGAR Backend (Stop):
```bash
az container stop --name germogli-backend-container --resource-group germogli-rg
```
**Resultado:** ✅ Detiene el backend y **PARA LA FACTURACIÓN**

### 📊 Ver Estado:
```bash
az container show --name germogli-backend-container --resource-group germogli-rg --query "instanceView.state" --output tsv
```
**Estados posibles:** `Running`, `Stopped`, `Pending`

### 📋 Ver Logs:
```bash
az container logs --name germogli-backend-container --resource-group germogli-rg
```

### 🔄 Reiniciar Backend:
```bash
az container restart --name germogli-backend-container --resource-group germogli-rg
```

---

## 💰 IMPORTANTE - AHORRO DE COSTOS

**Cuando el contenedor está STOPPED:**
- ❌ **NO se factura** compute (CPU/memoria)
- ✅ **Solo se factura** almacenamiento mínimo
- 📱 **El Application Gateway sigue funcionando** (necesario para SSL)
- ⏱️ **Tiempo de arranque:** ~2-3 minutos

**Recomendación para presentaciones:**
1. **Antes de la presentación:** `az container start`
2. **Después de la presentación:** `az container stop`
3. **Solo para desarrollo:** Mantener encendido temporalmente

---

## 🔄 ACTUALIZAR CÓDIGO Y REDESPLEGAR

### Paso 1: Construir nueva imagen Docker
```bash
# Construir imagen con cambios
docker build -t germogliregistry.azurecr.io/germogli-backend:latest .
```

### Paso 2: Subir imagen al registry
```bash
# Login al registry
az acr login --name germogliregistry

# Subir imagen
docker push germogliregistry.azurecr.io/germogli-backend:latest
```

### Paso 3: Detener contenedor actual
```bash
az container stop --name germogli-backend-container --resource-group germogli-rg
```

### Paso 4: Eliminar contenedor (para recrear con nueva imagen)
```bash
az container delete --name germogli-backend-container --resource-group germogli-rg --yes
```

### Paso 5: Recrear con nueva imagen
```bash
az container create --resource-group germogli-rg --file container-group-gateway-template.yaml
```

### Paso 6: Verificar que funciona
```bash
# Ver estado
az container show --name germogli-backend-container --resource-group germogli-rg --query "instanceView.state" --output tsv

# Ver logs
az container logs --name germogli-backend-container --resource-group germogli-rg

# Probar SSL
curl -I https://germogli-app.eastus.cloudapp.azure.com/auth/login
```

### 🚨 IMPORTANTE: Si cambia la IP del contenedor
```bash
# Obtener nueva IP
NEW_IP=$(az container show --name germogli-backend-container --resource-group germogli-rg --query "ipAddress.ip" --output tsv)
echo "Nueva IP: $NEW_IP"

# Actualizar Application Gateway
az network application-gateway address-pool update --gateway-name germogli-app-gateway --resource-group germogli-rg --name appGatewayBackendPool --servers $NEW_IP

# Actualizar health probe
az network application-gateway probe update --gateway-name germogli-app-gateway --resource-group germogli-rg --name healthProbe --host $NEW_IP
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

