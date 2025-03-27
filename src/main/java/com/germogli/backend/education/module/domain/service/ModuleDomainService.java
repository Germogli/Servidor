package com.germogli.backend.education.module.domain.service;

import com.germogli.backend.authentication.domain.model.UserDomain;
import com.germogli.backend.common.exception.ResourceNotFoundException;
import com.germogli.backend.education.module.application.dto.CreateModuleResponseDTO;
import com.germogli.backend.education.domain.service.EducationSharedService;
import com.germogli.backend.education.module.application.dto.ModuleResponseDTO;
import com.germogli.backend.education.module.domain.model.ModuleDomain;
import com.germogli.backend.education.module.domain.repository.ModuleDomainRepository;
import com.germogli.backend.education.tag.application.dto.TagResponseDTO;
import com.germogli.backend.education.tag.domain.model.TagDomain;
import com.germogli.backend.education.tag.domain.repository.TagDomainRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.List;

/**
 * Servicio de dominio para gestionar módulos educativos.
 *
 * Responsabilidades:
 * - Gestionar la lógica de negocio para módulos
 * - Validar permisos de usuarios
 * - Realizar operaciones CRUD de módulos
 * - Gestionar las etiquetas de los módulos
 */
@RequiredArgsConstructor
@Service
public class ModuleDomainService {

    // Repositorios para acceso a datos de módulos y etiquetas
    private final ModuleDomainRepository moduleDomainRepository;
    private final TagDomainRepository tagDomainRepository;

    // Servicio para gestionar operaciones compartidas de autenticación
    private final EducationSharedService educationSharedService;

    /**
     * Obtiene todos los modulos.
     *
     * @return Lista de modulos.
     * @throws ResourceNotFoundException si no hay modulos disponibles.
     */
    public List<ModuleDomain> getAllModules() {
        List<ModuleDomain> modules = moduleDomainRepository.getAll();
        if (modules.isEmpty()) {
            throw new ResourceNotFoundException("No hay modulos para mostrar");
        }
        return modules;
    }

    /**
     * Crea un nuevo modulo con validación de permisos y existencia de etiquetas.
     *
     * Pasos:
     * 1. Verificar si el usuario tiene rol de administrador
     * 2. Validar la existencia de todas las etiquetas proporcionadas
     * 3. Crear el módulo con la información proporcionada
     *
     * @param dto DTO con los datos para crear el modulo.
     * @return modulo creado.
     * @throws AccessDeniedException si el usuario no tiene permisos de administrador
     * @throws ResourceNotFoundException si alguna etiqueta no existe
     */
    public ModuleDomain createModule(CreateModuleResponseDTO dto) {
        UserDomain currentUser = educationSharedService.getAuthenticatedUser();

        // Verificar permisos de administrador
        if (!educationSharedService.hasRole(currentUser, "ADMINISTRADOR")) {
            throw new AccessDeniedException("El usuario no tiene permisos para crear módulos.");
        }

        // Verificar que todos los tags existen
        Set<TagDomain> tags = dto.getTagIds().stream()
                .map(tagId -> {
                    TagDomain tag = tagDomainRepository.getById(tagId);
                    if (tag == null) {
                        throw new ResourceNotFoundException("Tag no encontrado con ID: " + tagId);
                    }
                    return tag;
                })
                .collect(Collectors.toSet());

        ModuleDomain module = ModuleDomain.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .tags(tags)
                .creationDate(LocalDateTime.now())
                .build();

        return moduleDomainRepository.createModuleWithTags(module);
    }

    // Los demás métodos permanecen igual. Solo agregaré comentarios al método toResponse

    /**
     * Convierte un objeto de dominio ModuleDomain a un DTO de respuesta.
     *
     * Realiza el mapeo de:
     * - Identificador del módulo
     * - Título
     * - Descripción
     * - Fecha de creación
     * - Etiquetas asociadas
     *
     * @param module Objeto de dominio del módulo a convertir
     * @return DTO de respuesta con la información del módulo
     */
    public ModuleResponseDTO toResponse(ModuleDomain module) {
        Set<TagResponseDTO> tagResponses = module.getTags().stream()
                .map(tagDomain -> TagResponseDTO.builder()
                        .id(tagDomain.getTagId())
                        .name(tagDomain.getTagName())
                        .build())
                .collect(Collectors.toSet());

        return ModuleResponseDTO.builder()
                .moduleId(module.getModuleId())
                .title(module.getTitle())
                .description(module.getDescription())
                .creationDate(module.getCreationDate())
                .tags(tagResponses)
                .build();
    }
}