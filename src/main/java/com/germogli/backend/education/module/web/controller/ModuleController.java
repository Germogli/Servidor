package com.germogli.backend.education.module.web.controller;

import com.germogli.backend.education.module.application.dto.ApiResponseDTO;

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

    @GetMapping("/getAllModules")
    public ResponseEntity<ApiResponseDTO<List<ModuleDomain>>> getAllModules() {
        List<ModuleDomain> moduleList = moduleDomainService.getAllModulesWithTags();

        ApiResponseDTO<List<ModuleDomain>> response = ApiResponseDTO.<List<ModuleDomain>>builder()
                .message("Módulos recuperados correctamente")
                .data(moduleList)
                .build();

        return ResponseEntity.ok(response);
    }
}
