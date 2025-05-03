package com.germogli.backend.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración de CORS (Cross-Origin Resource Sharing) para la aplicación Spring Boot.
 * Esta clase permite manejar peticiones desde diferentes orígenes, especialmente desde
 * nuestro frontend de React que se ejecuta en un puerto diferente durante desarrollo.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    /**
     * Sobrescribe el método de configuración de mapeos CORS para definir
     * las reglas de acceso desde otros dominios.
     *
     * @param registry El registro de CORS donde se definen las configuraciones
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Aplica esta configuración a todas las rutas de la API
                .allowedOrigins(
                        "http://localhost:5173", // Permite solicitudes desde el servidor de desarrollo de React
                        "http://127.0.0.1:5500"  // Permite solicitudes desde Live Server (VS Code)
                )
                // Para producción podrías añadir: .allowedOrigins("https://miapp.com")
                // O permitir múltiples orígenes: .allowedOrigins("http://localhost:3000", "https://miapp.com")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS") // Métodos HTTP permitidos
                .allowedHeaders("*") // Permite todos los headers en las peticiones
                .exposedHeaders("Authorization") // Expone explícitamente la cabecera Authorization
                .allowCredentials(true) // Permite enviar cookies en solicitudes cross-origin (importante para autenticación)
                .maxAge(3600); // Duración en segundos que los navegadores pueden cachear la respuesta pre-flight
        // Valor que puede cambiar segun la etapa de desarrollo
    }
}
