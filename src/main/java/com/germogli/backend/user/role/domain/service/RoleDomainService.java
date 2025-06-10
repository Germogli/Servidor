package com.germogli.backend.user.role.domain.service;

import com.germogli.backend.authentication.domain.model.UserDomain;
import com.germogli.backend.authentication.domain.repository.UserDomainRepository;
import com.germogli.backend.common.exception.ResourceNotFoundException;
import com.germogli.backend.user.role.application.dto.UpdateUserRoleDTO;
import com.germogli.backend.user.role.domain.model.Role;
import com.germogli.backend.user.role.domain.repository.RoleDomainRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RoleDomainService {
    private final RoleDomainRepository roleDomainRepository;
    private final UserDomainRepository userDomainRepository;

    public void updateUserRole(UpdateUserRoleDTO dto) {
        // Se extrae el usuario autenticado
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();

        // Verificar que el usuario a modificar exista
        UserDomain currentUser = userDomainRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado para el username: " + username));

        // Se verifica que el usuario tenga rol administrador
        boolean isAdmin = currentUser.getRole() != null &&
                currentUser.getRole().getRoleType().equalsIgnoreCase("ADMINISTRADOR");

        // En caso de que no sea administrador se arroja una excepcion
        if (!isAdmin) {
            throw new AccessDeniedException("No tiene permisos para actualizar roles de usuario");
        }

        // Verificar que el rol a asignar sea válido
        if (dto.getRoleId() < 1 || dto.getRoleId() > 4) {
            throw new ResourceNotFoundException("Rol no encontrado con id: " + dto.getRoleId());
        }

        // Construir el objeto Role con el ID validado
        Role role = Role.builder()
                .id(dto.getRoleId())
                .build();

        roleDomainRepository.updateUserRole(dto.getUserId(), role);
    }
}