# Configuración para Desarrollo Local

## Requisitos previos

1. **MySQL instalado localmente**
   - MySQL 8.0 o superior
   - Servidor MySQL ejecutándose

2. **Base de datos creada**
   ```sql
   CREATE DATABASE germogli_db;
   ```

3. **Java 21** (requerido)
4. **Maven** (incluido con Maven Wrapper)

## Configuración rápida

### Paso 1: Configurar credenciales de base de datos

Edita el archivo `.env.local` según tu configuración de MySQL:

#### Para MySQL SIN contraseña (configuración típica de desarrollo):
```env
# Configuración de Base de Datos Local MySQL
DB_LOCAL_URL=jdbc:mysql://localhost:3306/germogli_db?allowPublicKeyRetrieval=true&useSSL=false
DB_LOCAL_USERNAME=root
# NO incluir línea DB_LOCAL_PASSWORD cuando no hay contraseña

# Configuración JWT para desarrollo local
JWT_SECRET=uX1v6U/7nO0t8aB3W9vZ6rT3yN0m+P7bQzS9vD8wX0cE
COOKIE_NAME=germogli_jwt

# Configuración adicional para desarrollo
SPRING_PROFILES_ACTIVE=local

# Azure Storage (reemplazar con tu connection string)
AZURE_CONNECTION_STRING=DefaultEndpointsProtocol=https;AccountName=tu_storage_account;AccountKey=TU_AZURE_STORAGE_KEY_AQUI;EndpointSuffix=core.windows.net

# Configuraciones del servidor
SERVER_PORT=8080
SERVER_ADDRESS=localhost
```

#### Para MySQL CON contraseña:
```env
# Configuración de Base de Datos Local MySQL
DB_LOCAL_URL=jdbc:mysql://localhost:3306/germogli_db?allowPublicKeyRetrieval=true&useSSL=false
DB_LOCAL_USERNAME=root
DB_LOCAL_PASSWORD=tu_contraseña_aqui

# Configuración JWT para desarrollo local
JWT_SECRET=uX1v6U/7nO0t8aB3W9vZ6rT3yN0m+P7bQzS9vD8wX0cE
COOKIE_NAME=germogli_jwt

# Configuración adicional para desarrollo
SPRING_PROFILES_ACTIVE=local

# Azure Storage (reemplazar con tu connection string)
AZURE_CONNECTION_STRING=DefaultEndpointsProtocol=https;AccountName=tu_storage_account;AccountKey=TU_AZURE_STORAGE_KEY_AQUI;EndpointSuffix=core.windows.net

# Configuraciones del servidor
SERVER_PORT=8080
SERVER_ADDRESS=localhost
```

⚠️ **IMPORTANTE**: 
- **SIN contraseña**: NO incluyas la línea `DB_LOCAL_PASSWORD`
- **CON contraseña**: SÍ incluye `DB_LOCAL_PASSWORD=tu_contraseña`

### Paso 2: Ajustar configuración de Spring Boot (si es necesario)

Si tu MySQL tiene contraseña, también debes editar `src/main/resources/application-local.properties`:

#### Para MySQL CON contraseña:
```properties
# Configuración de base de datos MySQL local (OBLIGATORIAS desde .env.local)
spring.datasource.url=${DB_LOCAL_URL}
spring.datasource.username=${DB_LOCAL_USERNAME}
spring.datasource.password=${DB_LOCAL_PASSWORD}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

#### Para MySQL SIN contraseña (configuración actual):
```properties
# Configuración de base de datos MySQL local (OBLIGATORIAS desde .env.local)
spring.datasource.url=${DB_LOCAL_URL}
spring.datasource.username=${DB_LOCAL_USERNAME}
# Sin contraseña para desarrollo local
spring.datasource.password=
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

## Pasos para ejecutar localmente

### 1. Clonar y navegar al proyecto
```bash
cd "Proyecto formativo/Servidor"
```

### 2. Ejecutar con perfil local

#### 🚀 Opción 1: Script automatizado (RECOMENDADO)
```powershell
# En Windows (PowerShell)
.\run-local.ps1
```

Este script:
- Carga automáticamente las variables del `.env.local`
- Ejecuta la aplicación con el perfil local
- Muestra información útil durante el inicio

#### Opción 2: Comando manual
```bash
# Con Maven Wrapper (recomendado)
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"

# O con Maven (si está instalado)
mvn spring-boot:run "-Dspring-boot.run.profiles=local"
```

#### Opción 3: Compilar y ejecutar JAR
```bash
.\mvnw.cmd clean package -DskipTests
java -jar target/*.jar --spring.profiles.active=local
```

### 3. Verificar que funciona
- ✅ La aplicación se ejecuta en: **http://localhost:8080**
- ✅ Los logs mostrarán: `Started BackendApplication in X.XXX seconds`
- ✅ Verás consultas SQL en tiempo real
- ✅ Las tablas se crearán automáticamente en la base de datos
- ✅ El perfil activo mostrará: `The following 1 profile is active: "local"`

## Configuración personalizada

Si necesitas cambiar la configuración de base de datos, edita el archivo:
`src/main/resources/application-local.properties`

Puedes cambiar:
- URL de la base de datos
- Usuario y contraseña
- Puerto del servidor
- Configuraciones de logging

## Solución de problemas

### ❌ Error: "Access denied for user 'root'@'localhost' (using password: YES)"

**Causa**: Configuración incorrecta de contraseña

**Solución**:
1. **Si tu MySQL NO tiene contraseña**:
   - Elimina la línea `DB_LOCAL_PASSWORD=` del `.env.local`
   - Asegúrate que `application-local.properties` tenga `spring.datasource.password=`

2. **Si tu MySQL SÍ tiene contraseña**:
   - Agrega `DB_LOCAL_PASSWORD=tu_contraseña` al `.env.local`
   - Cambia en `application-local.properties`: `spring.datasource.password=${DB_LOCAL_PASSWORD}`

### ❌ Error: "Could not obtain connection to query metadata"

**Causa**: MySQL no está ejecutándose o configuración incorrecta

**Solución**:
1. Verificar que MySQL esté ejecutándose
2. Verificar que la base de datos `germogli_db` exista:
   ```sql
   CREATE DATABASE IF NOT EXISTS germogli_db;
   ```
3. Verificar usuario y contraseña
4. Verificar puerto MySQL (por defecto 3306)

### ❌ Error: "Port 8080 was already in use"

**Solución**: Cambiar el puerto en `.env.local`:
```env
SERVER_PORT=8081
```

### ❌ Error: "mvn command not found" o "mvnw.cmd not found"

**Solución**: Usar el Maven Wrapper incluido:
```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```

### ❌ Variables de entorno no se cargan

**Solución**: Usar el script automatizado:
```powershell
.\run-local.ps1
```

### 🔧 Problemas con tablas

Si hay problemas con las tablas, cambiar en `application-local.properties`:
```properties
# Para recrear tablas cada inicio (útil en desarrollo)
spring.jpa.hibernate.ddl-auto=create-drop

# Para actualizar tablas automáticamente (recomendado)
spring.jpa.hibernate.ddl-auto=update
```

### 🐛 Debug y logs

Para ver más información de debug, agregar en `application-local.properties`:
```properties
# Logs detallados
logging.level.com.germogli=DEBUG
logging.level.org.springframework.web=DEBUG
logging.level.org.hibernate.SQL=DEBUG
```

## 💡 Consejos adicionales

### Verificar configuración
```powershell
# Ver variables cargadas por el script
.\run-local.ps1
# El script mostrará qué variables se configuraron
```

### Reiniciar aplicación rápidamente
- Usar `Ctrl+C` para parar
- Ejecutar `.un-local.ps1` nuevamente
- Spring Boot DevTools permite reinicio automático al cambiar código

### Probar la API
- URL base: http://localhost:8080
- Swagger UI (si está habilitado): http://localhost:8080/swagger-ui.html
- Actuator: http://localhost:8080/actuator/health

### Base de datos
- Ver tablas creadas en `germogli_db`
- Las tablas se crean automáticamente basadas en las entidades JPA
- Usar un cliente MySQL como MySQL Workbench o phpMyAdmin para gestionar datos

