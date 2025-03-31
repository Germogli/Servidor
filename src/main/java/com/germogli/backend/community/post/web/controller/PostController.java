package com.germogli.backend.community.post.web.controller;

import com.germogli.backend.community.application.dto.common.ApiResponseDTO;
import com.germogli.backend.community.post.application.dto.CreatePostRequestDTO;
import com.germogli.backend.community.post.application.dto.PostResponseDTO;
import com.germogli.backend.community.post.application.dto.UpdatePostRequestDTO;
import com.germogli.backend.community.post.domain.model.PostDomain;
import com.germogli.backend.community.post.domain.service.PostDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

/**
 * Controlador REST para la gestión de publicaciones en la comunidad.
 * Proporciona endpoints para crear, obtener, listar, actualizar y eliminar posts.
 */
@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class PostController {

    private final PostDomainService postDomainService;

    /**
     * Endpoint para crear una nueva publicación.
     *
     * @param request DTO con los datos para crear el post.
     * @return Respuesta API con el post creado.
     */
    @PostMapping
    public ResponseEntity<ApiResponseDTO<PostResponseDTO>> createPost(@Valid @RequestBody CreatePostRequestDTO request) {
        PostDomain post = postDomainService.createPost(request);
        return ResponseEntity.ok(ApiResponseDTO.<PostResponseDTO>builder()
                .message("Publicación creada correctamente")
                .data(postDomainService.toResponse(post))
                .build());
    }

    /**
     * Endpoint para obtener una publicación por su ID.
     *
     * @param id Identificador del post.
     * @return Respuesta API con el post encontrado.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<PostResponseDTO>> getPostById(@PathVariable Integer id) {
        PostDomain post = postDomainService.getPostById(id);
        return ResponseEntity.ok(ApiResponseDTO.<PostResponseDTO>builder()
                .message("Publicación recuperada correctamente")
                .data(postDomainService.toResponse(post))
                .build());
    }

    /**
     * Endpoint para listar todas las publicaciones.
     *
     * @return Respuesta API con la lista de posts.
     */
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<PostResponseDTO>>> getAllPosts() {
        List<PostResponseDTO> posts = postDomainService.toResponseList(postDomainService.getAllPosts());
        return ResponseEntity.ok(ApiResponseDTO.<List<PostResponseDTO>>builder()
                .message("Publicaciones recuperadas correctamente")
                .data(posts)
                .build());
    }

    /**
     * Endpoint para actualizar una publicación.
     *
     * @param id      Identificador del post a actualizar.
     * @param request DTO con los datos a actualizar.
     * @return Respuesta API con el post actualizado.
     */
    @PutMapping("/{id}")
    @PreAuthorize("@postSecurity.canUpdate(#id, principal)")
    public ResponseEntity<ApiResponseDTO<PostResponseDTO>> updatePost(@PathVariable Integer id,
                                                                      @Valid @RequestBody UpdatePostRequestDTO request) {
        PostDomain post = postDomainService.updatePost(id, request);
        return ResponseEntity.ok(ApiResponseDTO.<PostResponseDTO>builder()
                .message("Publicación actualizada correctamente")
                .data(postDomainService.toResponse(post))
                .build());
    }

    /**
     * Endpoint para eliminar una publicación.
     *
     * @param id Identificador del post a eliminar.
     * @return Respuesta API confirmando la eliminación.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("@postSecurity.canDelete(#id, principal)")
    public ResponseEntity<ApiResponseDTO<Void>> deletePost(@PathVariable Integer id) {
        postDomainService.deletePost(id);
        return ResponseEntity.ok(ApiResponseDTO.<Void>builder()
                .message("Publicación eliminada correctamente")
                .build());
    }
}
