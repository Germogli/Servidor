package com.germogli.backend.monitoring.domain.service;

import com.germogli.backend.authentication.domain.model.UserDomain;
import com.germogli.backend.authentication.domain.repository.UserDomainRepository;
import com.germogli.backend.common.exception.ResourceNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

/**
 * Servicio compartido para operaciones comunes dentro del módulo Monitoring.
 * Proporciona utilidades para obtener el usuario autenticado, validar roles
 * y verificar recursos relacionados.
 */
@Service
@RequiredArgsConstructor
public class MonitoringSharedService {

    private final UserDomainRepository userRepository;

    /**
     * Obtiene el usuario autenticado actual desde el contexto de seguridad.
     *
     * @return Usuario autenticado como entidad de dominio.
     * @throws ResourceNotFoundException si el usuario no se encuentra en el sistema.
     */
    public UserDomain getAuthenticatedUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con username: " + username));
    }

    /**
     * Verifica si un usuario posee un rol específico.
     *
     * @param userDomain Usuario a verificar.
     * @param role       Rol esperado (por ejemplo, "ADMINISTRADOR").
     * @return true si el usuario posee el rol indicado, false en caso contrario.
     */
    public boolean hasRole(UserDomain userDomain, String role) {
        return userDomain.getRole() != null &&
                userDomain.getRole().getRoleType().equalsIgnoreCase(role);
    }
}