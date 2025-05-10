package com.germogli.backend.authentication.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

/**
 * Filtro de autenticación JWT que extrae tokens de cookies HttpOnly.
 * Verifica y establece la autenticación en el contexto de seguridad.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final JwtCookieManager jwtCookieManager;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // Extraer el token JWT de las cookies
            String token = jwtCookieManager.extractJwtFromCookies(request);

            if (token != null) {
                // Obtener el username del token
                String username = jwtService.getUsernameFromToken(token);

                // Si el username es válido y no hay autenticación configurada, se procede
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    // Carga el UserDetails del usuario
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    // Verifica la validez del token
                    if (jwtService.isTokenValid(token, userDetails)) {
                        // Crea un objeto de autenticación y lo establece en el contexto de seguridad
                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);

                        // Para debugging
                        log.debug("Usuario autenticado via cookie: {}", username);
                    }
                }
            }
        } catch (Exception e) {
            log.error("No se pudo establecer la autenticación: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}