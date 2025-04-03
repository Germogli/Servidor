package com.germogli.backend.education.tag.web.controller;

import com.germogli.backend.education.application.dto.ApiResponseDTO;
import com.germogli.backend.education.tag.application.dto.TagResponseDTO;
import com.germogli.backend.education.tag.domain.model.TagDomain;
import com.germogli.backend.education.tag.domain.service.TagDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestionar etiquetas del módulo education.
 */
@RestController
@RequestMapping("/tags")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class TagController {

    private final TagDomainService tagDomainService;

    /**
     * Endpoint para crear una nueva etiqueta.
     * Recibe el nombre de la etiqueta vía path variable, la crea en el servicio y luego la mapea a DTO.
     *
     * @param tagName el nombre de la etiqueta a crear.
     * @return ApiResponseDTO con el TagResponseDTO creado.
     */
    @PostMapping("/{tagName}")
    public ResponseEntity<ApiResponseDTO<TagResponseDTO>> createTag(@PathVariable String tagName) {
        TagDomain tagDomain = tagDomainService.createTag(tagName);
        // Mapear el TagDomain a TagResponseDTO usando el método auxiliar
        TagResponseDTO tagResponse = tagDomainService.toResponse(tagDomain);
        // Retornar la respuesta con el objeto DTO
        return ResponseEntity.ok(ApiResponseDTO.<TagResponseDTO>builder()
                .message("Etiqueta creada correctamente")
                .data(tagResponse)
                .build());
    }

    /**
     * Endpoint para obtener una etiqueta por su nombre.
     * Busca la etiqueta en el servicio y la mapea a DTO para la respuesta.
     *
     * @param tagId id de la etiqueta a buscar.
     * @return ApiResponseDTO con el TagResponseDTO encontrado.
     */
    @GetMapping("/getTagId/{tagId}")
    public ResponseEntity<ApiResponseDTO<TagResponseDTO>> getTagById(@PathVariable Integer tagId) {
        TagDomain tagDomain = tagDomainService.getTagById(tagId);
        // Mapear el objeto de dominio a DTO
        TagResponseDTO tagResponse = tagDomainService.toResponse(tagDomain);
        // Retornar la respuesta con el DTO mapeado
        return ResponseEntity.ok(ApiResponseDTO.<TagResponseDTO>builder()
                .message("Etiqueta encontrada correctamente")
                .data(tagResponse)
                .build());
    }

    /**
     * Endpoint para obtener una etiqueta por su nombre.
     * Busca la etiqueta en el servicio y la mapea a DTO para la respuesta.
     *
     * @param tagName el nombre de la etiqueta a buscar.
     * @return ApiResponseDTO con el TagResponseDTO encontrado.
     */
    @GetMapping("/getTagName/{tagName}")
    public ResponseEntity<ApiResponseDTO<TagResponseDTO>> getTagByName(@PathVariable String tagName) {
        TagDomain tagDomain = tagDomainService.getTagByName(tagName);
        // Mapear el objeto de dominio a DTO
        TagResponseDTO tagResponse = tagDomainService.toResponse(tagDomain);
        // Retornar la respuesta con el DTO mapeado
        return ResponseEntity.ok(ApiResponseDTO.<TagResponseDTO>builder()
                .message("Etiqueta encontrada correctamente")
                .data(tagResponse)
                .build());
    }

    /**
     * Endpoint para eliminar una etiqueta por su id.
     *
     * @param tagId el identificador de la etiqueta a eliminar.
     * @return ApiResponseDTO sin datos (Void) confirmando la eliminación.
     */
    @DeleteMapping("/{tagId}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteTag(@PathVariable Integer tagId) {
        tagDomainService.deleteTagById(tagId);
        return ResponseEntity.ok(ApiResponseDTO.<Void>builder()
                .message("La etiqueta se eliminó correctamente")
                .build());
    }

    /**
     * Endpoint para actualizar el nombre de una etiqueta.
     * Recibe un DTO con la información de la etiqueta a actualizar, lo procesa en el servicio y mapea el resultado.
     *
     * @param dto el TagResponseDTO que contiene el ID y el nuevo nombre.
     * @return ApiResponseDTO con el TagResponseDTO actualizado.
     */
    @PutMapping
    public ResponseEntity<ApiResponseDTO<TagResponseDTO>> updateTagName(@RequestBody TagResponseDTO dto) {
        TagDomain updatedTag = tagDomainService.updateTagName(dto);
        // Mapear el objeto de dominio actualizado a DTO
        TagResponseDTO tagResponse = tagDomainService.toResponse(updatedTag);
        return ResponseEntity.ok(ApiResponseDTO.<TagResponseDTO>builder()
                .message("Etiqueta actualizada correctamente.")
                .data(tagResponse)
                .build());
    }

    /**
     * Endpoint para obtener todas las etiquetas.
     * Recupera la lista de TagDomain, la mapea a una lista de TagResponseDTO y la retorna.
     *
     * @return ApiResponseDTO con la lista de TagResponseDTO.
     */
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<TagResponseDTO>>> getAllTags() {
        List<TagDomain> tagDomains = tagDomainService.getAllTags();
        // Mapear la lista de dominio a lista de DTO usando el método auxiliar
        List<TagResponseDTO> tagResponseList = tagDomainService.toResponseList(tagDomains);
        return ResponseEntity.ok(ApiResponseDTO.<List<TagResponseDTO>>builder()
                .message("Todas las etiquetas encontradas correctamente.")
                .data(tagResponseList)
                .build());
    }

    /**
     * Endpoint para obtener o crear una etiqueta según su nombre.
     * Si la etiqueta ya existe, se retorna; si no, se crea y luego se retorna.
     *
     * @param tagName el nombre de la etiqueta.
     * @return ApiResponseDTO con el TagResponseDTO obtenido o creado.
     */
    @GetMapping("/getOrCreate/{tagName}")
    public ResponseEntity<ApiResponseDTO<TagResponseDTO>> getOrCreateTag(@PathVariable String tagName) {
        TagDomain tagDomain = tagDomainService.getOrCreateTag(tagName);
        // Mapea el objeto de dominio a DTO
        TagResponseDTO tagResponse = tagDomainService.toResponse(tagDomain);
        return ResponseEntity.ok(ApiResponseDTO.<TagResponseDTO>builder()
                .message("Etiqueta obtenida o creada correctamente")
                .data(tagResponse)
                .build());
    }
}
