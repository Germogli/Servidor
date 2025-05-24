package com.germogli.backend.authentication.web.config;

import com.germogli.backend.authentication.infrastructure.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Configuración de seguridad para la aplicación Spring Security.
 * Define reglas de autorización, filtros de seguridad y política de sesiones.
 */
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationProvider authProvider;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // ✅ ORÍGENES PERMITIDOS - Incluir Live Server y otros puertos comunes
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",   // React default
                "http://localhost:5173",   // Vite dev server
                "http://127.0.0.1:5173",   // Vite dev server alternativo
                "http://127.0.0.1:5500",   // ✅ Live Server VS Code
                "http://localhost:5500",   // ✅ Live Server alternativo
                "http://127.0.0.1:8080",   // ✅ Por si hay redirecciones locales
                "http://localhost:8080"    // ✅ Por si hay redirecciones locales
        ));

        // ✅ MÉTODOS HTTP PERMITIDOS
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));

        // ✅ HEADERS PERMITIDOS
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers",
                "Cookie"  // ✅ IMPORTANTE para cookies JWT
        ));

        // ✅ HEADERS EXPUESTOS (que el cliente puede leer)
        configuration.setExposedHeaders(Arrays.asList(
                "Set-Cookie",
                "Authorization"
        ));

        // ✅ PERMITIR CREDENCIALES (cookies, headers de autorización)
        configuration.setAllowCredentials(true);

        // ✅ DURACIÓN DE CACHE para respuestas preflight (1 hora)
        configuration.setMaxAge(3600L);

        // ✅ APLICAR A TODAS LAS RUTAS
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // ✅ ASEGURAR QUE EL FILTRO JWT MANEJE COOKIES CORRECTAMENTE
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/ws/**").permitAll() // ✅ IMPORTANTE: Permitir handshake WebSocket
                        .requestMatchers("/readings/device/**").permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}