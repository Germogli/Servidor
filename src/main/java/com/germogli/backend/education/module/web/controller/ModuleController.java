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
 * Proporciona endpoints para realizar operaciones CRUD sobre módulos.
 */
@RestController
@RequestMapping("/modules")
@RequiredArgsConstructor
public class ModuleController {

    private final ModuleDomainService moduleDomainService;

    /**
     * Recupera todos los módulos disponibles.
     *
     * @return ResponseEntity con una lista de módulos y un mensaje de éxito.
     */
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<ModuleResponseDTO>>> getAllModules() {
        List<ModuleResponseDTO> modules = moduleDomainService.toResponseList(moduleDomainService.getAllModules());
        return ResponseEntity.ok(ApiResponseDTO.<List<ModuleResponseDTO>>builder()
                .message("Módulos recuperados correctamente")
                .data(modules)
                .build());
    }

    /**
     * Crea un nuevo módulo en el sistema.
     *
     * @param moduleDTO Datos del módulo a crear.
     * @return ResponseEntity con el módulo creado y un mensaje de éxito.
     */
    @PostMapping
    public ResponseEntity<ApiResponseDTO<ModuleResponseDTO>> createModule(@RequestBody CreateModuleResponseDTO moduleDTO) {
        ModuleDomain module = moduleDomainService.createModule(moduleDTO);
        return ResponseEntity.ok(ApiResponseDTO.<ModuleResponseDTO>builder()
                .message("Módulo creado correctamente")
                .data(moduleDomainService.toResponse(module))
                .build());
    }

    /**
     * Actualiza un módulo existente identificado por su ID.
     *
     * @param moduleId Identificador único del módulo a actualizar.
     * @param moduleDTO Datos para actualizar el módulo.
     * @return ResponseEntity con el módulo actualizado y un mensaje de éxito.
     */
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

    /**
     * Recupera un módulo específico por su identificador.
     *
     * @param moduleId Identificador único del módulo.
     * @return ResponseEntity con el módulo encontrado y un mensaje de éxito.
     */
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
     * @param tagIds Lista de IDs de etiquetas para filtrar los módulos.
     * @return ResponseEntity con la lista de módulos encontrados y un mensaje de éxito.
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
     * @param moduleId Identificador único del módulo a eliminar.
     * @return ResponseEntity con un mensaje de éxito después de eliminar el módulo.
     */
    @DeleteMapping("/{moduleId}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteModule(@PathVariable Integer moduleId) {
        moduleDomainService.deleteModule(moduleId);

        return ResponseEntity.ok(ApiResponseDTO.<Void>builder()
                .message("Módulo eliminado correctamente")
                .build());
    }
}