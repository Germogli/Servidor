-- Script de inicializacion de la base de datos Germogli
-- Este script se ejecuta automaticamente cuando se crea el contenedor MySQL

-- Crear la base de datos si no existe
CREATE DATABASE IF NOT EXISTS germogli CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Usar la base de datos
USE germogli;

-- Otorgar permisos al usuario germogli
GRANT ALL PRIVILEGES ON germogli.* TO 'germogli'@'%';
FLUSH PRIVILEGES;

-- Mensaje de confirmacion
SELECT 'Base de datos Germogli inicializada correctamente' AS message;

