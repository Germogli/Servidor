package com.germogli.backend.education.module.domain.service;

import com.germogli.backend.authentication.domain.model.UserDomain;
import com.germogli.backend.authentication.domain.repository.UserDomainRepository;
import com.germogli.backend.common.exception.ResourceNotFoundException;
import com.germogli.backend.education.module.application.dto.ModuleResponseDTO;
import com.germogli.backend.education.module.domain.model.ModuleDomain;
import com.germogli.backend.education.module.domain.repository.ModuleDomainRepository;
import com.germogli.backend.education.module.infrastructure.crud.EducationModuleCrudRepository;
import com.germogli.backend.education.module.infrastructure.entity.ModuleEntity;

import com.germogli.backend.education.tag.domain.model.TagDomain;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import java.util.stream.Collectors; // Importar Collectors


import java.util.List;

/**
 * Servicio de dominio para los modulos.
 * Contiene la lógica de negocio.
 */
@RequiredArgsConstructor
@Service
public class ModuleDomainService {

    private final EducationModuleCrudRepository moduleCrudRepository;
    private final ModuleDomainRepository moduleDomainRepository;
    private final UserDomainRepository userDomainRepository;

    public List<ModuleDomain> getAllModulesWithTags() {
        // Se obtiene la lista de entidades
        List<ModuleEntity> moduleEntities = moduleCrudRepository.findAll();

        // se usa el metodo fromEntities() para convertirlos a objetos de dominio
        return ModuleDomain.fromEntities(moduleEntities);
    }

    // Metodo para crear un modulo con etiquetas
    public ModuleDomain createModuleWithTags(ModuleDomain moduleDomain) {
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

        // Validar que el título no esté vacío
        if (moduleDomain.getTitle() == null || moduleDomain.getTitle().trim().isEmpty()) {
            throw new ResourceNotFoundException("El título del módulo no puede estar vacío");
        }

        // Validar que la descripción no esté vacía
        if (moduleDomain.getDescription() == null || moduleDomain.getDescription().trim().isEmpty()) {
            throw new ResourceNotFoundException("La descripción del módulo no puede estar vacía");
        }

        // Validar que la lista de etiquetas no esté vacía
        if (moduleDomain.getTags() == null || moduleDomain.getTags().isEmpty()) {
            throw new ResourceNotFoundException("Debe proporcionar al menos una etiqueta para el módulo");
        }

        // Delegar la creación al repositorio
        return moduleDomainRepository.createModuleWithTags(moduleDomain);
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
        return ModuleResponseDTO.builder()
                .moduleId(module.getModuleId())            // Mapea moduleId del dominio al id del DTO
                .title(module.getTitle())                  // Mapea el título del módulo
                .description(module.getDescription())      // Mapea la descripción del módulo
                .creationDate(module.getCreationDate())    // Mapea la fecha de creación
                .tagNames(module.getTags().stream()         // Mapea las etiquetas
                        .map(TagDomain::getTagName)            // Obtiene solo los nombres de las etiquetas
                        .collect(Collectors.toSet()))          // Recolecta en un Set de Strings
                .build();
    }

}
