package com.germogli.backend.monitoring.crop.web.controller;

import com.germogli.backend.monitoring.application.dto.common.ApiResponseDTO;
import com.germogli.backend.monitoring.crop.application.dto.CropRequestDTO;
import com.germogli.backend.monitoring.crop.application.dto.CropResponseDTO;
import com.germogli.backend.monitoring.crop.domain.model.CropDomain;
import com.germogli.backend.monitoring.crop.domain.service.CropDomainService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de cultivos.
 * Proporciona endpoints para crear, obtener, actualizar y eliminar cultivos.
 */
@RestController
@RequestMapping("/crops")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class CropController {

    private final CropDomainService cropDomainService;

    /**
     * Endpoint para crear un nuevo cultivo.
     *
     * @param request DTO con los datos del cultivo.
     * @return Respuesta API con el cultivo creado.
     */
    @PostMapping
    public ResponseEntity<ApiResponseDTO<CropResponseDTO>> createCrop(@Valid @RequestBody CropRequestDTO request) {
        CropDomain crop = cropDomainService.createCrop(request);
        return ResponseEntity.ok(ApiResponseDTO.<CropResponseDTO>builder()
                .message("Cultivo creado correctamente")
                .data(cropDomainService.toResponse(crop))
                .build());
    }

    /**
     * Endpoint para obtener un cultivo por su ID.
     *
     * @param id Identificador del cultivo.
     * @return Respuesta API con el cultivo encontrado.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<CropResponseDTO>> getCropById(@PathVariable Integer id) {
        CropDomain crop = cropDomainService.getCropById(id);
        return ResponseEntity.ok(ApiResponseDTO.<CropResponseDTO>builder()
                .message("Cultivo recuperado correctamente")
                .data(cropDomainService.toResponse(crop))
                .build());
    }

    /**
     * Endpoint para listar todos los cultivos del usuario autenticado.
     * Si el usuario es administrador, puede obtener todos los cultivos.
     *
     * @return Respuesta API con la lista de cultivos.
     */
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<CropResponseDTO>>> getUserCrops() {
        List<CropResponseDTO> crops = cropDomainService.toResponseList(cropDomainService.getUserCrops());
        return ResponseEntity.ok(ApiResponseDTO.<List<CropResponseDTO>>builder()
                .message("Cultivos recuperados correctamente")
                .data(crops)
                .build());
    }

    /**
     * Endpoint para actualizar un cultivo.
     *
     * @param id Identificador del cultivo a actualizar.
     * @param request DTO con los nuevos datos.
     * @return Respuesta API con el cultivo actualizado.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<CropResponseDTO>> updateCrop(
            @PathVariable Integer id, @Valid @RequestBody CropRequestDTO request) {
        CropDomain crop = cropDomainService.updateCrop(id, request);
        return ResponseEntity.ok(ApiResponseDTO.<CropResponseDTO>builder()
                .message("Cultivo actualizado correctamente")
                .data(cropDomainService.toResponse(crop))
                .build());
    }

    /**
     * Endpoint para eliminar un cultivo.
     *
     * @param id Identificador del cultivo a eliminar.
     * @return Respuesta API confirmando la eliminación.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteCrop(@PathVariable Integer id) {
        cropDomainService.deleteCrop(id);
        return ResponseEntity.ok(ApiResponseDTO.<Void>builder()
                .message("Cultivo eliminado correctamente")
                .build());
    }
}