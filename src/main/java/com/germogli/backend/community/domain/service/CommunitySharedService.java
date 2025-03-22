package com.germogli.backend.community.domain.service;

import com.germogli.backend.authentication.domain.model.UserDomain;
import com.germogli.backend.authentication.domain.repository.UserDomainRepository;
import com.germogli.backend.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/**
 * Servicio compartido para operaciones comunes en el módulo Community.
 * Permite obtener el usuario autenticado y verificar roles.
 */
@Service
@RequiredArgsConstructor
public class CommunitySharedService {

    private final UserDomainRepository userRepository;

    /**
     * Obtiene el usuario autenticado actual.
     *
     * @return Usuario del dominio.
     * @throws ResourceNotFoundException si no se encuentra el usuario.
     */
    public UserDomain getAuthenticatedUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));
    }

    /**
     * Verifica si el usuario posee un rol específico.
     *
     * @param userDomain Usuario del dominio.
     * @param role       Rol a verificar (por ejemplo, "ADMINISTRADOR").
     * @return true si el usuario tiene el rol indicado, false en caso contrario.
     */
    public boolean hasRole(UserDomain userDomain, String role) {
        return userDomain.getRole() != null && userDomain.getRole().getRoleType().equalsIgnoreCase(role);
    }
}
