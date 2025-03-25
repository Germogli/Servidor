package com.germogli.backend.education.module.domain.service;

import com.germogli.backend.education.module.application.dto.ModuleResponseDTO;
import com.germogli.backend.education.module.domain.model.ModuleDomain;
import com.germogli.backend.education.module.infrastructure.crud.EducationModuleCrudRepository;
import com.germogli.backend.education.module.infrastructure.entity.ModuleEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio de dominio para los modulos.
 * Contiene la lógica de negocio.
 */
@RequiredArgsConstructor
@Service
public class ModuleDomainService {

    private final EducationModuleCrudRepository moduleCrudRepository;

    public List<ModuleDomain> getAllModulesWithTags() {
        // Se obtiene la lista de entidades
        List<ModuleEntity> moduleEntities = moduleCrudRepository.findAll();

        // se usa el metodo fromEntities() para convertirlos a objetos de dominio
        return ModuleDomain.fromEntities(moduleEntities);
    }

    // Método auxiliar para convertir lista de dominios a lista de DTOs de respuesta
    public List<ModuleResponseDTO> toResponseList(List<ModuleDomain> moduleDomains) {
        return ModuleResponseDTO.fromDomains(moduleDomains);
    }
}
