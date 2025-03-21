package com.germogli.backend.education.tag.web.controller;

import com.germogli.backend.education.application.dto.ApiResponseDTO;
import com.germogli.backend.education.tag.domain.model.TagDomain;
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

    // Endpoint para crear una nueva etiqueta
    @PostMapping("/create/{tagName}")
    public ResponseEntity<ApiResponseDTO<TagDomain>> createTag(@PathVariable String tagName) {
        // Crear la etiqueta utilizando el servicio
        TagDomain tagDomain = tagDomainService.createTag(tagName);

        // Retornar la respuesta con el objeto de la etiqueta creada
        return ResponseEntity.ok(ApiResponseDTO.<TagDomain>builder()
                .message("Etiqueta creada correctamente")
                .data(tagDomain)
                .build());
    }

    // Endpoint para obtener una etiqueta por su nombre
    @GetMapping("/get/{tagName}")
    public ResponseEntity<ApiResponseDTO<TagDomain>> getTagByName(@PathVariable String tagName) {
        // Buscar la etiqueta por nombre utilizando el servicio
        TagDomain tagDomain = tagDomainService.getTagByName(tagName);

        // Retornar la respuesta con los datos de la etiqueta encontrada
        return ResponseEntity.ok(ApiResponseDTO.<TagDomain>builder()
                .message("Etiqueta encontrada correctamente")
                .data(tagDomain)
                .build());
    }

    // Endpoint para eliminar una etiqueta por su id
    @DeleteMapping("/{tagId}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteTag(@PathVariable Integer tagId) {
        tagDomainService.deleteTagById(tagId);
        return ResponseEntity.ok(ApiResponseDTO.<Void>builder()
                .message("La etiqueta se eliminó correctamente")
                .build());
    }
}
