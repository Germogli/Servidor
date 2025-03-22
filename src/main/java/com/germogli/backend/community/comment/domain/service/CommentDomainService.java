package com.germogli.backend.community.comment.domain.service;

import com.germogli.backend.authentication.domain.model.UserDomain;
import com.germogli.backend.common.notification.NotificationPublisher;
import com.germogli.backend.community.comment.domain.model.CommentDomain;
import com.germogli.backend.community.comment.domain.repository.CommentDomainRepository;
import com.germogli.backend.community.comment.application.dto.CreateCommentRequestDTO;
import com.germogli.backend.community.comment.application.dto.CommentResponseDTO;
import com.germogli.backend.community.domain.service.CommunitySharedService;
import com.germogli.backend.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de dominio para la gestión de comentarios.
 * Contiene la lógica de negocio para crear, obtener, listar y eliminar comentarios.
 */
@Service
@RequiredArgsConstructor
public class CommentDomainService {

    // Repositorio para operaciones de persistencia de comentarios (usando procedimientos almacenados)
    private final CommentDomainRepository commentRepository;
    // Servicio compartido para obtener el usuario autenticado y verificar roles.
    private final CommunitySharedService sharedService;
    // Servicio para enviar notificaciones.
    private final NotificationPublisher notificationPublisher;

    /**
     * Crea un comentario utilizando los datos del DTO.
     *
     * @param request DTO con la información para crear el comentario.
     * @return El comentario creado.
     */
    public CommentDomain createComment(CreateCommentRequestDTO request) {
        UserDomain currentUser = sharedService.getAuthenticatedUser();
        CommentDomain comment = CommentDomain.builder()
                .postId(request.getPostId())
                .userId(currentUser.getId())
                .content(request.getContent())
                .threadId(request.getThreadId())
                .groupId(request.getGroupId())
                .build();
        CommentDomain savedComment = commentRepository.save(comment);

        // Notifica la creación del comentario (por ejemplo, al autor del post)
        notificationPublisher.publishNotification(
                currentUser.getId(),
                "Se ha agregado un comentario en el post",
                "comment"
        );

        return savedComment;
    }

    /**
     * Obtiene un comentario por su ID.
     *
     * @param id Identificador del comentario.
     * @return El comentario encontrado.
     * @throws ResourceNotFoundException si el comentario no existe.
     */
    public CommentDomain getCommentById(Integer id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comentario no encontrado con id: " + id));
    }

    /**
     * Obtiene todos los comentarios.
     *
     * @return Lista de comentarios.
     * @throws ResourceNotFoundException si no hay comentarios disponibles.
     */
    public List<CommentDomain> getAllComments() {
        List<CommentDomain> comments = commentRepository.findAll();
        if (comments.isEmpty()) {
            throw new ResourceNotFoundException("No hay comentarios disponibles.");
        }
        return comments;
    }

    /**
     * Elimina un comentario.
     * Verifica que el usuario autenticado sea el propietario o tenga rol de ADMINISTRADOR.
     *
     * @param id Identificador del comentario a eliminar.
     * @throws AccessDeniedException si el usuario no tiene permisos.
     */
    public void deleteComment(Integer id) {
        UserDomain currentUser = sharedService.getAuthenticatedUser();
        CommentDomain comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comentario no encontrado con id: " + id));
        boolean isOwner = comment.getUserId().equals(currentUser.getId());
        boolean isAdmin = sharedService.hasRole(currentUser, "ADMINISTRADOR");
        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("No tiene permisos para eliminar este comentario");
        }
        commentRepository.deleteById(id);

        // Notifica la eliminación del comentario
        notificationPublisher.publishNotification(
                currentUser.getId(),
                "Su comentario ha sido eliminado",
                "comment"
        );
    }

    /**
     * Convierte un objeto CommentDomain en un CommentResponseDTO para enviar en la API.
     *
     * @param comment Comentario a convertir.
     * @return DTO con la información del comentario.
     */
    public CommentResponseDTO toResponse(CommentDomain comment) {
        return CommentResponseDTO.builder()
                .id(comment.getId())
                .postId(comment.getPostId())
                .userId(comment.getUserId())
                .content(comment.getContent())
                .threadId(comment.getThreadId())
                .groupId(comment.getGroupId())
                .creationDate(comment.getCreationDate())
                .build();
    }

    /**
     * Convierte una lista de CommentDomain en una lista de CommentResponseDTO.
     *
     * @param comments Lista de comentarios.
     * @return Lista de DTOs con la información de los comentarios.
     */
    public List<CommentResponseDTO> toResponseList(List<CommentDomain> comments) {
        return comments.stream().map(this::toResponse).collect(Collectors.toList());
    }
}
