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

    /**
     * Configura la cadena de filtros de seguridad con las reglas de acceso específicas.
     * Define qué endpoints requieren autenticación y cuáles están disponibles públicamente.
     *
     * @param http Configuración de seguridad HTTP
     * @return Cadena de filtros de seguridad configurada
     * @throws Exception Si ocurre un error durante la configuración
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable()) // Desactiva CSRF para simplificar la autenticación basada en tokens
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // Permite solicitudes preflight OPTIONS
                        .requestMatchers("/auth/**").permitAll() // Permite el acceso público a endpoints de autenticación
                        .requestMatchers("/ws/**").permitAll() // Permite el acceso al endpoint WebSocket
                        .anyRequest().authenticated() // Resto de solicitudes requieren autenticación
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Política sin estado
                .authenticationProvider(authProvider) // Configura el AuthenticationProvider
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class) // Agrega el filtro JWT
                .build();
    }
}