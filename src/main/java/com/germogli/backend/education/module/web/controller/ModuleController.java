package com.germogli.backend.education.module.web.controller;

import com.germogli.backend.education.application.dto.ApiResponseDTO;
import com.germogli.backend.education.module.application.dto.ModuleResponseDTO;
import com.germogli.backend.education.module.domain.model.ModuleDomain;
import com.germogli.backend.education.module.domain.service.ModuleDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestionar los módulos en el módulo Education.
 */
@RestController
@RequestMapping("/modules")
@RequiredArgsConstructor
public class ModuleController {

    private final ModuleDomainService moduleDomainService;

    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<ModuleResponseDTO>>> getAllModules() {
        List<ModuleDomain> moduleDomains = moduleDomainService.getAllModulesWithTags();
        // Mapear la lista de dominio a lista de DTO usando el método auxiliar
        List<ModuleResponseDTO> moduleResponseList = moduleDomainService.toResponseList(moduleDomains);

        return ResponseEntity.ok(ApiResponseDTO.<List<ModuleResponseDTO>>builder()
                .message("Módulos recuperados correctamente")
                .data(moduleResponseList)
                .build());
    }

    @PostMapping
    public ResponseEntity<ApiResponseDTO<ModuleResponseDTO>> createModule(@RequestBody ModuleResponseDTO moduleDTO) {
        // Convertimos el DTO a Domain antes de pasarlo al servicio
        ModuleDomain createdModule = moduleDomainService.createModuleWithTags(moduleDTO.toDomain());

        // Convertimos el dominio creado de vuelta a DTO para la respuesta
        ModuleResponseDTO responseDTO = ModuleResponseDTO.fromDomain(createdModule);

        return ResponseEntity.ok(ApiResponseDTO.<ModuleResponseDTO>builder()
                .message("Módulo creado correctamente")
                .data(responseDTO)
                .build());
    }

}