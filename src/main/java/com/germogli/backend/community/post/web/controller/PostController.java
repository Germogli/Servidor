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
import org.springframework.web.multipart.MultipartFile;

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
     * Endpoint para crear una nueva publicación que admite archivo opcional.
     * Si se adjunta un archivo (foto o video), se sube a Azure Blob Storage en el contenedor "publicaciones"
     * y se almacena la URL generada en el campo multimediaContent.
     *
     * @param postRequest Objeto JSON con los datos de la publicación.
     * @param file Archivo opcional (foto o video).
     * @return Respuesta API con la publicación creada.
     */
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<ApiResponseDTO<PostResponseDTO>> createPost(
            @RequestPart("post") @Valid CreatePostRequestDTO postRequest,
            @RequestPart(name = "file", required = false) MultipartFile file) {

        PostDomain post = postDomainService.createPost(postRequest, file);
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
     * Solo el propietario o un administrador pueden actualizar el post.
     *
     * @param id Identificador del post a actualizar.
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
     * Solo el propietario o un administrador pueden eliminar el post.
     * Además, si la publicación tiene contenido multimedia, se elimina el archivo del contenedor "publicaciones" en Azure Blob Storage.
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
