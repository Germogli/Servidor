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
        validateAdminPermission();

        if (tagName == null || tagName.trim().isEmpty()) {
            throw new ResourceNotFoundException("El nombre de la etiqueta no puede estar vacío.");
        }

        TagDomain existingTag = tagDomainRepository.getByName(tagName);
        if (existingTag != null) {
            throw new ResourceNotFoundException("La etiqueta ya existe, por favor cree otra");
        }

        Integer tagId = tagDomainRepository.getOrCreateTag(tagName);
        return tagDomainRepository.getById(tagId);
    }

    public TagDomain getTagById(Integer tagId) {
        validateAdminPermission();

        if (tagId == null) {
            throw new ResourceNotFoundException("El ID de la etiqueta no puede estar vacío.");
        }

        TagDomain existsTag = tagDomainRepository.getById(tagId);
        if (existsTag == null) {
            throw new ResourceNotFoundException("No existe una etiqueta con el id: " + tagId);
        }

        return tagDomainRepository.getById(tagId);
    }

    public TagDomain getTagByName(String tagName) {
        validateAdminPermission();

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
        validateAdminPermission();

        TagDomain existTag = tagDomainRepository.getById(id);
        if (existTag == null) {
            throw new ResourceNotFoundException("No existe una etiqueta para eliminar correspondiente al id:" + id);
        }

        tagDomainRepository.deleteById(id);
    }

    public TagDomain updateTagName(TagResponseDTO dto) {
        validateAdminPermission();

        if (dto == null || dto.getId() == null || dto.getName() == null || dto.getName().isEmpty()) {
            throw new IllegalArgumentException("El ID y el nombre de la etiqueta son requeridos.");
        }

        // Verificar que la etiqueta exista antes de intentar actualizarla
        TagDomain existingTag = tagDomainRepository.getById(dto.getId());
        if (existingTag == null) {
            throw new ResourceNotFoundException("Etiqueta no encontrada con el ID: " + dto.getId());
        }

        TagDomain tag = TagDomain.builder()
                .tagId(dto.getId())
                .tagName(dto.getName())
                .build();

        tagDomainRepository.updateTagName(tag);
        return tagDomainRepository.getById(dto.getId());
    }

    public List<TagDomain> getAllTags() {
        List<TagDomain> tags = tagDomainRepository.findAll();
        if (tags.isEmpty()) {
            throw new ResourceNotFoundException("No hay etiquetas disponibles.");
        }
        return tags;
    }

    public TagDomain getOrCreateTag(String tagName) {
        validateAdminPermission();

        if (tagName == null || tagName.trim().isEmpty()) {
            throw new ResourceNotFoundException("El nombre de la etiqueta no puede estar vacío.");
        }

        // En caso de existir utiliza el metodo para obtener la etiqueta mediante el id
        Integer tagId = tagDomainRepository.getOrCreateTag(tagName);
        return tagDomainRepository.getById(tagId);
    }

    private void validateAdminPermission() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();

        UserDomain currentUser = userDomainRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado para el username: " + username));

        boolean isAdmin = currentUser.getRole() != null &&
                currentUser.getRole().getRoleType().equalsIgnoreCase("ADMINISTRADOR");

        if (!isAdmin) {
            throw new AccessDeniedException("No tiene permisos para realizar esta acción.");
        }
    }

    public TagResponseDTO toResponse(TagDomain tag) {
        return TagResponseDTO.builder()
                .id(tag.getTagId())
                .name(tag.getTagName())
                .build();
    }

    public List<TagResponseDTO> toResponseList(List<TagDomain> tags) {
        return tags.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
