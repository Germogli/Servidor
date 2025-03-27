package com.germogli.backend.education.module.domain.service;

import com.germogli.backend.authentication.domain.model.UserDomain;
import com.germogli.backend.common.exception.ResourceNotFoundException;
import com.germogli.backend.education.module.application.dto.CreateModuleResponseDTO;
import com.germogli.backend.education.domain.service.EducationSharedService;
import com.germogli.backend.education.module.application.dto.ModuleResponseDTO;
import com.germogli.backend.education.module.application.dto.UpdateModuleRequestDTO;
import com.germogli.backend.education.module.domain.model.ModuleDomain;
import com.germogli.backend.education.module.domain.repository.ModuleDomainRepository;
import com.germogli.backend.education.tag.application.dto.TagResponseDTO;
import com.germogli.backend.education.tag.domain.model.TagDomain;
import com.germogli.backend.education.tag.domain.repository.TagDomainRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors; // Importar Collectors


import java.util.List;

/**
 * Servicio de dominio para los modulos.
 * Contiene la lógica de negocio.
 */
@RequiredArgsConstructor
@Service
public class ModuleDomainService {

    // Repositorio para operaciones de persistencia de modulos y etiqutas.
    private final ModuleDomainRepository moduleDomainRepository;
    private final TagDomainRepository tagDomainRepository;
    // Servicio compartido para obtener el usuario autenticado y verificar roles.
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
     * Crea un nuevo modulo.
     *
     * @param dto DTO con los datos para crear el modulo.
     * @return modulo creada.
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
                    TagDomain tag = tagDomainRepository.getById(tagId);  // Este método debería devolver null si no encuentra la etiqueta
                    if (tag == null) {
                        throw new ResourceNotFoundException("Tag no encontrado con ID: " + tagId);  // Lanzamos la excepción si es null
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

    /**
     * Actualiza un módulo existente.
     *
     * @param moduleId ID del módulo a actualizar.
     * @param dto DTO con los nuevos datos del módulo.
     * @return El módulo actualizado.
     */
    public ModuleDomain updateModule(Integer moduleId, UpdateModuleRequestDTO dto) {
        UserDomain currentUser = educationSharedService.getAuthenticatedUser();

        // Verificar permisos de administrador
        if (!educationSharedService.hasRole(currentUser, "ADMINISTRADOR")) {
            throw new AccessDeniedException("El usuario no tiene permisos para actualizar módulos.");
        }

        // Verificar que el módulo existe
        ModuleDomain existingModule = moduleDomainRepository.getById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Módulo no encontrado con ID: " + moduleId));

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

        // Crear objeto actualizado del módulo
        ModuleDomain updatedModule = ModuleDomain.builder()
                .moduleId(moduleId)
                .title(dto.getTitle())
                .description(dto.getDescription())
                .tags(tags)
                .creationDate(existingModule.getCreationDate()) // Mantener la fecha original
                .build();

        // Llamar al repositorio para ejecutar el SP
        return moduleDomainRepository.updateModuleWithTags(updatedModule);
    }

    /**
     * Obtiene una publicación por su ID.
     *
     * @param id Identificador del modulo.
     * @return modulo encontrado.
     * @throws ResourceNotFoundException si no se encuentra el post.
     */
    public ModuleDomain getModuleById(Integer id) {
        return moduleDomainRepository.getById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Modulo no encontrado con id: " + id));
    }

    /**
     * Filtra los módulos por las etiquetas proporcionadas.
     *
     * @param tagIds Lista de IDs de etiquetas.
     * @return Lista de módulos que coinciden con las etiquetas proporcionadas.
     * @throws ResourceNotFoundException si no se encuentran módulos con las etiquetas proporcionadas.
     */
    public List<ModuleDomain> filterModulesByTags(List<Integer> tagIds) {
        List<ModuleDomain> filteredModules = moduleDomainRepository.filterModulesByTags(tagIds);

        if (filteredModules.isEmpty()) {
            throw new ResourceNotFoundException("No se encontraron módulos con las etiquetas proporcionadas.");
        }

        return filteredModules;
    }

    // Método auxiliar para convertir lista de dominios a lista de DTOs de respuesta
    public List<ModuleResponseDTO> toResponseList(List<ModuleDomain> moduleDomains) {
        return ModuleResponseDTO.fromDomains(moduleDomains);
    }

    /**
     * Método auxiliar para convertir un objeto ModuleDomain a ModuleResponseDTO.
     * Este método transforma el modelo de dominio del módulo en el DTO de respuesta.
     *
     * @param module El objeto ModuleDomain que se desea convertir.
     * @return El objeto ModuleResponseDTO con la información mapeada.
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
