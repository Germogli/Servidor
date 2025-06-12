# Etapa 1: Construcción
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copiar archivos de configuración de Maven
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
COPY src ./src

# Dar permisos de ejecución al wrapper de Maven
RUN chmod +x ./mvnw

# Construir la aplicación
RUN ./mvnw clean package -DskipTests

# Etapa 2: Ejecución
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copiar el JAR generado desde la etapa de construcción
COPY --from=builder /app/target/*.jar app.jar

# Exponer los puertos HTTP y HTTPS
EXPOSE 8080 8443

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]