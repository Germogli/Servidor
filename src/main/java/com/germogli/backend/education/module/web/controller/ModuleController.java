package com.germogli.backend.education.module.web.controller;

import com.germogli.backend.education.application.dto.ApiResponseDTO;
import com.germogli.backend.education.module.application.dto.CreateModuleResponseDTO;
import com.germogli.backend.education.module.application.dto.ModuleResponseDTO;
import com.germogli.backend.education.module.application.dto.UpdateModuleRequestDTO;
import com.germogli.backend.education.module.domain.model.ModuleDomain;
import com.germogli.backend.education.module.domain.service.ModuleDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

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
        List<ModuleResponseDTO> modules = moduleDomainService.toResponseList(moduleDomainService.getAllModules());
        return ResponseEntity.ok(ApiResponseDTO.<List<ModuleResponseDTO>>builder()
                .message("Módulos recuperados correctamente")
                .data(modules)
                .build());
    }

    @PostMapping
    public ResponseEntity<ApiResponseDTO<ModuleResponseDTO>> createModule(@RequestBody CreateModuleResponseDTO moduleDTO) {
        ModuleDomain module = moduleDomainService.createModule(moduleDTO);
        return ResponseEntity.ok(ApiResponseDTO.<ModuleResponseDTO>builder()
                .message("Módulo creado correctamente")
                .data(moduleDomainService.toResponse(module))
                .build());
    }

    @PutMapping("/{moduleId}")
    public ResponseEntity<ApiResponseDTO<ModuleResponseDTO>> updateModule(
            @PathVariable Integer moduleId,
            @RequestBody UpdateModuleRequestDTO moduleDTO)
    {
        ModuleDomain updatedModule = moduleDomainService.updateModule(moduleId, moduleDTO);
        return ResponseEntity.ok(ApiResponseDTO.<ModuleResponseDTO>builder()
                .message("Módulo actualizado correctamente")
                .data(moduleDomainService.toResponse(updatedModule))
                .build());
    }

    @GetMapping("/{moduleId}")
    public ResponseEntity<ApiResponseDTO<ModuleResponseDTO>> getModuleById(@PathVariable Integer moduleId) {
        ModuleDomain module = moduleDomainService.getModuleById(moduleId);
        return ResponseEntity.ok(ApiResponseDTO.<ModuleResponseDTO>builder()
                .message("Módulo encontrado correctamente")
                .data(moduleDomainService.toResponse(module))
                .build());
    }

    /**
     * Obtiene los módulos asociados a una o más etiquetas.
     *
     * @param tagIds Lista de IDs de etiquetas.
     * @return Lista de módulos encontrados.
     */
    @GetMapping("/filter")
    public ResponseEntity<ApiResponseDTO<List<ModuleResponseDTO>>> getModulesByTags(@RequestParam List<Integer> tagIds) {
        List<ModuleDomain> modules = moduleDomainService.filterModulesByTags(tagIds);

        return ResponseEntity.ok(ApiResponseDTO.<List<ModuleResponseDTO>>builder()
                .message("Módulos encontrados.")
                .data(modules.stream()
                        .map(moduleDomainService::toResponse)
                        .collect(Collectors.toList()))
                .build());
    }

    /**
     * Elimina un módulo por su ID.
     *
     * @param moduleId ID del módulo a eliminar.
     * @return Respuesta con mensaje de éxito.
     */
    @DeleteMapping("/{moduleId}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteModule(@PathVariable Integer moduleId) {
        moduleDomainService.deleteModule(moduleId);

        return ResponseEntity.ok(ApiResponseDTO.<Void>builder()
                .message("Módulo eliminado correctamente")
                .build());
    }

}