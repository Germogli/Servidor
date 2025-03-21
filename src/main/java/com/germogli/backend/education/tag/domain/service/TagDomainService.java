package com.germogli.backend.education.tag.domain.service;

import com.germogli.backend.authentication.domain.model.UserDomain;
import com.germogli.backend.authentication.domain.repository.UserDomainRepository;
import com.germogli.backend.common.exception.ResourceNotFoundException;
import com.germogli.backend.education.tag.application.dto.CreateTagRequestDTO;
import com.germogli.backend.education.tag.domain.repository.TagDomainRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class TagDomainService {
    private final TagDomainRepository tagDomainRepository;
    private final UserDomainRepository userDomainRepository;

    public Integer getOrCreateTag(CreateTagRequestDTO dto) {
        //Se extrae el usuario autenticado
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();

        // Verificar que el usuario exista
        UserDomain currentUser = userDomainRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado para el username: " + username));

        // se verifica que el usuario tenga el rol administrador
        boolean isAdmin = currentUser.getRole() != null &&
                currentUser.getRole().getRoleType().equalsIgnoreCase("ADMINISTRADOR");

        // En caso de que no sea administrador se arroja una excepcion
        if (!isAdmin) {
            throw new AccessDeniedException("No tiene permisos para actualizar roles de usuario");
        }

        // Obtener el nombre de la etiqueta desde el DTO
        String tagName = dto.getName();

        // Buscar la etiqueta por nombre
        Integer tagId = tagDomainRepository.getTagByName(tagName);

        // si la etiqueta no existe se crea
        if (tagId == null) {
            tagId = tagDomainRepository.createTag(tagName);
        }

        return tagId;
    }
}
