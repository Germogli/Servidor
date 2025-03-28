package com.germogli.backend.education.guides.web.controller;

import com.germogli.backend.education.application.dto.ApiResponseDTO;
import com.germogli.backend.education.guides.application.dto.CreateGuideRequestDTO;
import com.germogli.backend.education.guides.application.dto.GuideResponseDTO;
import com.germogli.backend.education.guides.application.dto.UpdateGuideRequestDTO;
import com.germogli.backend.education.guides.domain.model.GuideDomain;
import com.germogli.backend.education.guides.domain.service.GuideDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestionar las guías en el módulo Education.
 */
@RestController
@RequestMapping("/guides")
@RequiredArgsConstructor
public class GuideController {

    private final GuideDomainService guideDomainService;

    /**
     * Método para actualizar los datos de una guía.
     * Recibe una solicitud HTTP PUT con los datos necesarios para actualizar la guía.
     *
     * @param guideDTO Objeto que contiene los datos para actualizar la guía (título, descripción, etc.)
     * @return ResponseEntity con el resultado de la actualización de la guía, incluyendo un mensaje y los detalles de la guía actualizada.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<GuideResponseDTO>> updateGuideInfo(
            @PathVariable Integer id,
            @RequestBody UpdateGuideRequestDTO guideDTO
    ) {
        GuideDomain updatedGuide = guideDomainService.updateGuide(id, guideDTO);
        return ResponseEntity.ok(
                ApiResponseDTO.<GuideResponseDTO>builder()
                        .message("Guia actualizada correctamente")
                        .data(guideDomainService.toResponse(updatedGuide))
                        .build()
        );
    }

    /**
     * Obtiene todas las guías disponibles.
     *
     * @return ResponseEntity con la lista de guías en un ApiResponseDTO.
     */
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<GuideResponseDTO>>> getAllGuides() {
        List<GuideResponseDTO> guides = guideDomainService.toResponseList(guideDomainService.getAllGuides());
        return ResponseEntity.ok(
                ApiResponseDTO.<List<GuideResponseDTO>>builder()
                        .message("Guías recuperadas correctamente")
                        .data(guides)
                        .build()
        );
    }

    /**
     * Obtiene las guías pertenecientes a un módulo específico según su ID.
     *
     * @param id ID del módulo para el que se desean obtener las guías.
     * @return ResponseEntity con la lista de guías en un ApiResponseDTO.
     */
    @GetMapping("/getByModuleId/{id}")
    public ResponseEntity<ApiResponseDTO<List<GuideResponseDTO>>> getGuidesByModuleId(@PathVariable Integer id) {
        List<GuideResponseDTO> guides = guideDomainService.toResponseList(guideDomainService.getGuidesByModuleId(id));
        return ResponseEntity.ok(
                ApiResponseDTO.<List<GuideResponseDTO>>builder()
                        .message("Guías recuperadas correctamente para el modulo con id " + id)
                        .data(guides)
                        .build()
        );
    }

    /**
     * Obtiene una guía específica según su ID.
     *
     * @param id ID de la guía que se desea obtener.
     * @return ResponseEntity con los detalles de la guía en un ApiResponseDTO.
     */
    @GetMapping("/getById/{id}")
    public ResponseEntity<ApiResponseDTO<GuideResponseDTO>> getGuideById(@PathVariable Integer id) {
        GuideDomain guide = guideDomainService.getGuideById(id);
        return ResponseEntity.ok(
                ApiResponseDTO.<GuideResponseDTO>builder()
                        .message("Guía recuperada correctamente con ID " + id)
                        .data(GuideResponseDTO.fromDomain(guide))
                        .build()
        );
    }

    /**
     * Método para crear una nueva guía.
     * Recibe una solicitud HTTP POST con los datos necesarios para crear una guía.
     *
     * @param createGuideRequestDTO Objeto que contiene los datos para crear la guía (título, descripción, archivo PDF)
     * @return ResponseEntity con el resultado de la creación de la guía, incluyendo un mensaje y los detalles de la guía creada.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE) // Indica que el método maneja solicitudes POST con archivos (multipart/form-data)
    public ResponseEntity<ApiResponseDTO<GuideResponseDTO>> createGuide(
            @ModelAttribute CreateGuideRequestDTO createGuideRequestDTO // Se usa @ModelAttribute para bindear los datos del formulario
    ) {
        GuideDomain createdGuide = guideDomainService.createGuide(createGuideRequestDTO);

        return ResponseEntity.ok(
                ApiResponseDTO.<GuideResponseDTO>builder()
                        .message("Guía creada correctamente")
                        .data(GuideResponseDTO.fromDomain(createdGuide))
                        .build()
        );
    }
}
