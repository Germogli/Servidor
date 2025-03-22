package com.germogli.backend.education.tag.domain.service;

import com.germogli.backend.authentication.domain.model.UserDomain;
import com.germogli.backend.authentication.domain.repository.UserDomainRepository;
import com.germogli.backend.common.exception.ResourceNotFoundException;
import com.germogli.backend.education.tag.application.dto.TagResponseDTO;
import com.germogli.backend.education.tag.domain.model.TagDomain;
import com.germogli.backend.education.tag.domain.repository.TagDomainRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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

        // En caso de que no sea administrador se arroja una excepción
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

        // Verificar que el usuario exista
        UserDomain currentUser = userDomainRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado para el username: " + username));

        // Verifica que el usuario tenga rol administrador
        boolean isAdmin = currentUser.getRole() != null &&
                currentUser.getRole().getRoleType().equalsIgnoreCase("ADMINISTRADOR");

        if (!isAdmin) {
            throw new AccessDeniedException("No tiene permisos para actualizar roles de usuario");
        }

        if (tagName == null || tagName.trim().isEmpty()) {
            throw new ResourceNotFoundException("El nombre de la etiqueta no puede estar vacío.");
        }

        TagDomain tag = tagDomainRepository.getByName(tagName);
        if (tag == null) {
            throw new ResourceNotFoundException("Etiqueta no encontrada con el nombre: " + tagName);
        }

        return tag;
    }

    public void deleteTagById(Integer id) {
        // Se extrae el usuario autenticado
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();

        // Verificar que el usuario exista
        UserDomain currentUser = userDomainRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado para el username: " + username));

        // Verifica que el usuario tenga rol administrador
        boolean isAdmin = currentUser.getRole() != null &&
                currentUser.getRole().getRoleType().equalsIgnoreCase("ADMINISTRADOR");

        if (!isAdmin) {
            throw new AccessDeniedException("No tiene permisos para actualizar roles de usuario");
        }

        tagDomainRepository.deleteById(id);
    }

    public TagDomain updateTagName(TagResponseDTO dto) {
        // Se extrae el usuario autenticado
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();

        // Verificar que el usuario exista
        UserDomain currentUser = userDomainRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado para el username: " + username));

        // Verifica que el usuario tenga rol administrador
        boolean isAdmin = currentUser.getRole() != null &&
                currentUser.getRole().getRoleType().equalsIgnoreCase("ADMINISTRADOR");

        if (!isAdmin) {
            throw new AccessDeniedException("No tiene permisos para actualizar roles de usuario");
        }

        // Validar que el DTO contenga la información requerida
        if (dto == null || dto.getId() == null || dto.getName() == null || dto.getName().isEmpty()) {
            throw new IllegalArgumentException("El ID y el nombre de la etiqueta son requeridos.");
        }

        // Construir el objeto de dominio TagDomain a partir del DTO
        TagDomain tag = TagDomain.builder()
                .tagId(dto.getId())       // Asigna el ID del DTO al dominio
                .tagName(dto.getName())   // Asigna el nombre del DTO al dominio
                .build();

        // Actualiza el nombre de la etiqueta en el repositorio
        tagDomainRepository.updateTagName(tag);

        // Recupera el objeto actualizado para retornarlo
        TagDomain updatedTag = tagDomainRepository.getByName(dto.getName());

        return updatedTag;
    }

    public List<TagDomain> getAllTags() {
        return tagDomainRepository.findAll();
    }

    /**
     * Método de mapeo para convertir un objeto TagDomain a TagResponseDTO.
     * Este método transforma el modelo de dominio de la etiqueta en el DTO que se enviará en la respuesta.
     *
     * @param tag el objeto TagDomain que se desea convertir.
     * @return el objeto TagResponseDTO con la información mapeada.
     */
    public TagResponseDTO toResponse(TagDomain tag) {
        // Usamos el builder del DTO para asignar cada propiedad del TagDomain
        return TagResponseDTO.builder()
                .id(tag.getTagId())       // Mapea tagId (dominio) al campo id del DTO
                .name(tag.getTagName())   // Mapea tagName (dominio) al campo name del DTO
                .build();
    }

    /**
     * Método auxiliar para mapear una lista de TagDomain a una lista de TagResponseDTO.
     * Utiliza el método toResponse de cada objeto TagDomain.
     *
     * @param tags la lista de objetos TagDomain.
     * @return la lista de objetos TagResponseDTO.
     */
    public List<TagResponseDTO> toResponseList(List<TagDomain> tags) {
        // Convierte cada TagDomain de la lista a TagResponseDTO usando el método toResponse
        return tags.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
