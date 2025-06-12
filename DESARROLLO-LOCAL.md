# Configuración para Desarrollo Local

## Requisitos previos

1. **MySQL instalado localmente**
   - Instalar MySQL 8.0 o superior
   - Crear usuario y contraseña según tu configuración local

2. **Base de datos creada**
   ```sql
   CREATE DATABASE germogli_db;
   ```

3. **Java 17 o superior**
4. **Maven 3.6 o superior**

## Configuración de variables de entorno (RECOMENDADO)

### Opción 1: Usar archivo .env.local (MÁS FÁCIL)
Ya existe un archivo `.env.local` con valores por defecto. Si necesitas cambiar credenciales:

1. Edita el archivo `.env.local`
2. Cambia los valores de base de datos según tu configuración:
```
DB_LOCAL_USERNAME=tu_usuario
DB_LOCAL_PASSWORD=tu_contraseña
DB_LOCAL_URL=jdbc:mysql://localhost:3306/tu_base_datos
```

### Opción 2: Variables de entorno en terminal

#### En Windows (PowerShell):
```powershell
$env:DB_LOCAL_USERNAME="tu_usuario"
$env:DB_LOCAL_PASSWORD="tu_contraseña"
$env:DB_LOCAL_URL="jdbc:mysql://localhost:3306/tu_base_datos"
```

#### En Windows (CMD):
```cmd
set DB_LOCAL_USERNAME=tu_usuario
set DB_LOCAL_PASSWORD=tu_contraseña
set DB_LOCAL_URL=jdbc:mysql://localhost:3306/tu_base_datos
```

#### En Linux/Mac:
```bash
export DB_LOCAL_USERNAME="tu_usuario"
export DB_LOCAL_PASSWORD="tu_contraseña"
export DB_LOCAL_URL="jdbc:mysql://localhost:3306/tu_base_datos"
```

**Valores por defecto** (en .env.local):
- Usuario: `root`
- Contraseña: `root`
- URL: `jdbc:mysql://localhost:3306/germogli_db`

## Pasos para ejecutar localmente

### 1. Clonar y navegar al proyecto
```bash
cd "Proyecto formativo/Servidor"
```

### 2. Ejecutar con perfil local
```bash
# Opción 1: Con Maven
mvn spring-boot:run -Dspring-boot.run.profiles=local

# Opción 2: Con Java después de compilar
mvn clean package -DskipTests
java -jar target/*.jar --spring.profiles.active=local
```

### 3. Verificar que funciona
- La aplicación se ejecuta en: http://localhost:8080
- Los logs mostrarán las consultas SQL
- Las tablas se crearán automáticamente en la base de datos

## Configuración personalizada

Si necesitas cambiar la configuración de base de datos, edita el archivo:
`src/main/resources/application-local.properties`

Puedes cambiar:
- URL de la base de datos
- Usuario y contraseña
- Puerto del servidor
- Configuraciones de logging

## Solución de problemas

### Error de conexión a MySQL
1. Verificar que MySQL esté ejecutándose
2. Verificar usuario y contraseña
3. Verificar que la base de datos `germogli_db` exista

### Puerto ocupado
Cambiar el puerto en `application-local.properties`:
```properties
server.port=8081
```

### Problemas con tablas
Si hay problemas con las tablas, cambiar en `application-local.properties`:
```properties
spring.jpa.hibernate.ddl-auto=create-drop
```
(Esto recreará las tablas cada vez)

