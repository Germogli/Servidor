package com.germogli.backend.community.post.web.controller;

import com.germogli.backend.community.application.dto.common.ApiResponseDTO;
import com.germogli.backend.community.post.application.dto.CreatePostRequestDTO;
import com.germogli.backend.community.post.application.dto.PostResponseDTO;
import com.germogli.backend.community.post.application.dto.UpdatePostRequestDTO;
import com.germogli.backend.community.post.domain.model.PostDomain;
import com.germogli.backend.community.post.domain.service.PostDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
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
     * @param postRequest Objeto con los datos de la publicación.
     * @return Respuesta API con la publicación creada.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponseDTO<PostResponseDTO>> createPost(
            @ModelAttribute @Valid CreatePostRequestDTO postRequest) {

        PostDomain post = postDomainService.createPost(postRequest);

        return ResponseEntity.ok(ApiResponseDTO.<PostResponseDTO>builder()
                .message("Publicación creada correctamente")
                .data(postDomainService.toResponse(post))
                .build());
    }

    /**
     * Endpoint para actualizar una publicación con soporte para actualización de archivo multimedia.
     *
     * @param id Identificador de la publicación a actualizar
     * @param updateRequest DTO con los datos a actualizar
     * @param file Archivo multimedia opcional
     * @return Respuesta API con la publicación actualizada
     */
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@postSecurity.canUpdate(#id, principal)")
    public ResponseEntity<ApiResponseDTO<PostResponseDTO>> updatePost(
            @PathVariable Integer id,
            @ModelAttribute @Valid UpdatePostRequestDTO updateRequest,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        PostDomain post = postDomainService.updatePost(id, updateRequest, file);
        return ResponseEntity.ok(ApiResponseDTO.<PostResponseDTO>builder()
                .message("Publicación actualizada correctamente")
                .data(postDomainService.toResponse(post))
                .build());
    }

    /**
     * Endpoint para obtener una publicación por su ID.
     *
     * @param id Identificador del post.
     * @return Respuesta API con la publicación encontrada.
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
     * Endpoint para listar publicaciones de un grupo específico.
     *
     * @param groupId ID del grupo
     * @return Respuesta API con la lista de publicaciones del grupo
     */
    @GetMapping("/by-group/{groupId}")
    public ResponseEntity<ApiResponseDTO<List<PostResponseDTO>>> getPostsByGroupId(@PathVariable Integer groupId) {
        List<PostResponseDTO> posts = postDomainService.toResponseList(
                postDomainService.getPostsByGroupId(groupId));
        return ResponseEntity.ok(ApiResponseDTO.<List<PostResponseDTO>>builder()
                .message("Publicaciones del grupo recuperadas correctamente")
                .data(posts)
                .build());
    }

    /**
     * Endpoint para listar publicaciones de un usuario específico.
     * Si no se proporciona userId, se usará el usuario autenticado.
     *
     * @param userId ID del usuario (opcional)
     * @return Respuesta API con la lista de publicaciones del usuario
     */
    @GetMapping("/by-user")
    public ResponseEntity<ApiResponseDTO<List<PostResponseDTO>>> getPostsByUserId(
            @RequestParam(required = false) Integer userId) {
        List<PostResponseDTO> posts = postDomainService.toResponseList(
                postDomainService.getPostsByUserId(userId));
        return ResponseEntity.ok(ApiResponseDTO.<List<PostResponseDTO>>builder()
                .message("Publicaciones del usuario recuperadas correctamente")
                .data(posts)
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

    /**
     * Endpoint para listar publicaciones de un hilo específico.
     *
     * @param threadId ID del hilo
     * @return Respuesta API con la lista de publicaciones del hilo
     */
    @GetMapping("/by-thread/{threadId}")
    public ResponseEntity<ApiResponseDTO<List<PostResponseDTO>>> getPostsByThreadId(@PathVariable Integer threadId) {
        List<PostResponseDTO> posts = postDomainService.toResponseList(
                postDomainService.getPostsByThreadId(threadId));
        return ResponseEntity.ok(ApiResponseDTO.<List<PostResponseDTO>>builder()
                .message("Publicaciones del hilo recuperadas correctamente")
                .data(posts)
                .build());
    }
}