package com.germogli.backend.authentication.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

/**
 * Filtro de autenticación JWT que se ejecuta una sola vez por cada solicitud.
 * Extrae y valida el token JWT, y configura la autenticación en el contexto de seguridad.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // Extrae el token JWT del encabezado de la solicitud
        final String token = getTokenFromRequest(request);
        if (token != null) {
            // Obtiene el username del token
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
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    /**
     * Extrae el token JWT del encabezado Authorization.
     *
     * @param request Solicitud HTTP.
     * @return Token JWT o null si no está presente.
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
