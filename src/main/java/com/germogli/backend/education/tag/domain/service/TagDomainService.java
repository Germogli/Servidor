package com.germogli.backend.education.tag.domain.service;

import com.germogli.backend.authentication.domain.model.UserDomain;
import com.germogli.backend.authentication.domain.repository.UserDomainRepository;
import com.germogli.backend.common.exception.ResourceNotFoundException;
import com.germogli.backend.education.tag.domain.model.TagDomain;
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

    public TagDomain createTag(String tagName) {
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

        // Verificamos que el nombre de la etiqueta no esté vacío o nulo
        if (tagName == null || tagName.trim().isEmpty()) {
            throw new ResourceNotFoundException("El nombre de la etiqueta no puede estar vacío.");
        }

        TagDomain tagExists = tagDomainRepository.getByName(tagName);
        if (tagExists != null) {
            throw new ResourceNotFoundException("Ya existe una etiqueta con el nombre: " + tagName);
        }

        return tagDomainRepository.save(tagName);
    }

    public TagDomain getTagByName(String tagName) {
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

        // Verificar que el nombre de la etiqueta no sea nulo o vacío
        if (tagName == null || tagName.trim().isEmpty()) {
            throw new ResourceNotFoundException("El nombre de la etiqueta no puede estar vacío.");
        }

        // Llamar al repositorio para obtener la etiqueta por nombre
        TagDomain tag = tagDomainRepository.getByName(tagName);

        // Si no se encuentra la etiqueta, lanzar una excepción
        if (tag == null) {
            throw new ResourceNotFoundException("Etiqueta no encontrada con el nombre: " + tagName);
        }

        return tag;
    }

    public void deleteTagById(Integer id) {
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

        tagDomainRepository.deleteById(id);
    }
}
