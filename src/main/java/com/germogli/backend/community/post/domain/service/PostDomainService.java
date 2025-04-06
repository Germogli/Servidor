package com.germogli.backend.community.post.domain.service;

import com.germogli.backend.authentication.domain.model.UserDomain;
import com.germogli.backend.common.notification.application.service.NotificationService;
import com.germogli.backend.community.post.domain.model.PostDomain;
import com.germogli.backend.community.post.domain.repository.PostDomainRepository;
import com.germogli.backend.community.post.application.dto.CreatePostRequestDTO;
import com.germogli.backend.community.post.application.dto.PostResponseDTO;
import com.germogli.backend.community.post.application.dto.UpdatePostRequestDTO;
import com.germogli.backend.community.domain.service.CommunitySharedService;
import com.germogli.backend.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de dominio para la gestión de publicaciones.
 * Contiene la lógica para crear, obtener, listar, actualizar y eliminar posts.
 */
@Service
@RequiredArgsConstructor
public class PostDomainService {

    private final PostDomainRepository postRepository;
    private final CommunitySharedService sharedService;
    private final NotificationService notificationService;

    /**
     * Crea una nueva publicación.
     *
     * @param request DTO con los datos para crear el post.
     * @return Publicación creada.
     */
    public PostDomain createPost(CreatePostRequestDTO request) {
        UserDomain currentUser = sharedService.getAuthenticatedUser();
        PostDomain post = PostDomain.builder()
                .userId(currentUser.getId())
                .postType(request.getPostType())
                .content(request.getContent())
                .multimediaContent(request.getMultimediaContent())
                .groupId(request.getGroupId())
                .threadId(request.getThreadId())
                .postDate(LocalDateTime.now())
                .build();

        return postRepository.save(post); // No se envía notificación en creación
    }

    /**
     * Obtiene una publicación por su ID.
     *
     * @param id Identificador del post.
     * @return Publicación encontrada.
     * @throws ResourceNotFoundException si no se encuentra el post.
     */
    public PostDomain getPostById(Integer id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post no encontrado con id: " + id));
    }

    /**
     * Obtiene todas las publicaciones.
     *
     * @return Lista de posts.
     * @throws ResourceNotFoundException si no hay publicaciones disponibles.
     */
    public List<PostDomain> getAllPosts() {
        List<PostDomain> posts = postRepository.findAll();
        if (posts.isEmpty()) {
            throw new ResourceNotFoundException("No hay publicaciones disponibles.");
        }
        return posts;
    }

    /**
     * Actualiza una publicación.
     * Solo el propietario o un administrador pueden actualizar el post.
     *
     * @param id      Identificador del post a actualizar.
     * @param request DTO con los datos a actualizar.
     * @return Publicación actualizada.
     * @throws AccessDeniedException si el usuario no tiene permisos.
     */
    @Transactional
    public PostDomain updatePost(Integer id, UpdatePostRequestDTO request) {
        UserDomain currentUser = sharedService.getAuthenticatedUser();

        PostDomain existingPost = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post no encontrado con id: " + id));

        boolean isOwner = existingPost.getUserId().equals(currentUser.getId());
        boolean isAdmin = sharedService.hasRole(currentUser, "ADMINISTRADOR");

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("No tiene permisos para actualizar esta publicación.");
        }

        existingPost.setPostType(request.getPostType());
        existingPost.setContent(request.getContent());
        existingPost.setMultimediaContent(request.getMultimediaContent());
        existingPost.setPostDate(LocalDateTime.now());

        PostDomain updatedPost = postRepository.save(existingPost);

        // Notificar solo si el usuario es el propietario (evitar notificación duplicada si es admin)
        if (isOwner) {
            notificationService.sendNotification(
                    currentUser.getId(),
                    "Se ha actualizado tu publicación.",
                    "post"
            );
        }

        return updatedPost;
    }

    /**
     * Elimina una publicación.
     * Solo el propietario o un administrador pueden eliminar el post.
     *
     * @param id Identificador del post a eliminar.
     * @throws AccessDeniedException si el usuario no tiene permisos.
     */
    @Transactional
    public void deletePost(Integer id) {
        UserDomain currentUser = sharedService.getAuthenticatedUser();

        PostDomain post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post no encontrado con id: " + id));

        boolean isOwner = post.getUserId().equals(currentUser.getId());
        boolean isAdmin = sharedService.hasRole(currentUser, "ADMINISTRADOR");

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("No tiene permisos para eliminar esta publicación.");
        }

        postRepository.deleteById(id);

        // Notificar al propietario del post (aunque sea eliminado por un admin)
        notificationService.sendNotification(
                post.getUserId(),
                "Tu publicación ha sido eliminada.",
                "post"
        );
    }

    /**
     * Convierte un objeto PostDomain en un DTO de respuesta.
     *
     * @param post Publicación a convertir.
     * @return DTO con la información del post.
     */
    public PostResponseDTO toResponse(PostDomain post) {
        return PostResponseDTO.builder()
                .id(post.getId())
                .userId(post.getUserId())
                .postType(post.getPostType())
                .content(post.getContent())
                .multimediaContent(post.getMultimediaContent())
                .postDate(post.getPostDate())
                .groupId(post.getGroupId())
                .threadId(post.getThreadId())
                .build();
    }

    /**
     * Convierte una lista de PostDomain en una lista de DTOs de respuesta.
     *
     * @param posts Lista de publicaciones.
     * @return Lista de DTOs.
     */
    public List<PostResponseDTO> toResponseList(List<PostDomain> posts) {
        return posts.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
