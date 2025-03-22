package com.germogli.backend.community.comment.web.controller;

import com.germogli.backend.community.application.dto.common.ApiResponseDTO;
import com.germogli.backend.community.comment.application.dto.CreateCommentRequestDTO;
import com.germogli.backend.community.comment.application.dto.CommentResponseDTO;
import com.germogli.backend.community.comment.domain.model.CommentDomain;
import com.germogli.backend.community.comment.domain.service.CommentDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

/**
 * Controlador REST para la gestión de comentarios en Community.
 * Proporciona endpoints para crear, obtener, listar y eliminar comentarios.
 */
@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentDomainService commentDomainService;

    /**
     * Crea un nuevo comentario.
     *
     * @param request Datos para crear el comentario.
     * @return Respuesta API con el comentario creado.
     */
    @PostMapping
    public ResponseEntity<ApiResponseDTO<CommentResponseDTO>> createComment(@Valid @RequestBody CreateCommentRequestDTO request) {
        CommentDomain comment = commentDomainService.createComment(request);
        return ResponseEntity.ok(ApiResponseDTO.<CommentResponseDTO>builder()
                .message("Comentario creado correctamente")
                .data(commentDomainService.toResponse(comment))
                .build());
    }

    /**
     * Obtiene un comentario por su ID.
     *
     * @param id Identificador del comentario.
     * @return Respuesta API con el comentario encontrado.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<CommentResponseDTO>> getCommentById(@PathVariable Integer id) {
        CommentDomain comment = commentDomainService.getCommentById(id);
        return ResponseEntity.ok(ApiResponseDTO.<CommentResponseDTO>builder()
                .message("Comentario recuperado correctamente")
                .data(commentDomainService.toResponse(comment))
                .build());
    }

    /**
     * Obtiene todos los comentarios.
     *
     * @return Respuesta API con la lista de comentarios.
     */
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<CommentResponseDTO>>> getAllComments() {
        List<CommentResponseDTO> comments = commentDomainService.toResponseList(commentDomainService.getAllComments());
        return ResponseEntity.ok(ApiResponseDTO.<List<CommentResponseDTO>>builder()
                .message("Comentarios recuperados correctamente")
                .data(comments)
                .build());
    }

    /**
     * Elimina un comentario por su ID.
     *
     * @param id Identificador del comentario a eliminar.
     * @return Respuesta API confirmando la eliminación.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteComment(@PathVariable Integer id) {
        commentDomainService.deleteComment(id);
        return ResponseEntity.ok(ApiResponseDTO.<Void>builder()
                .message("Comentario eliminado correctamente")
                .build());
    }
}
