package com.germogli.backend.education.tag.web.controller;

import com.germogli.backend.education.application.dto.ApiResponseDTO;
import com.germogli.backend.education.tag.application.dto.CreateTagRequestDTO;
import com.germogli.backend.education.tag.domain.service.TagDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para gestionar etiquetas del modulo education.
 */
@RestController
@RequestMapping("/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagDomainService tagDomainService;

    /**
     * Endpoint para obtener o crear una etiqueta.
     * Solo accesible para usuarios con el rol ADMINISTRADOR.
     */
    @PostMapping
    public ResponseEntity<ApiResponseDTO<Integer>> getOrCreateTag(@RequestBody CreateTagRequestDTO dto) {
        Integer tagId = tagDomainService.getOrCreateTag(dto);
        return ResponseEntity.ok(ApiResponseDTO.<Integer>builder()
                .message("Etiqueta obtenida o creada correctamente")
                .data(tagId)
                .build());
    }

}
