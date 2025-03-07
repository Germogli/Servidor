package com.germogli.backend.community.post.domain.service;

import com.germogli.backend.community.post.application.dto.CreatePostRequestDTO;
import com.germogli.backend.community.post.application.dto.PostResponseDTO;
import com.germogli.backend.community.post.application.dto.UpdatePostRequestDTO;
import com.germogli.backend.community.post.domain.model.PostDomain;
import com.germogli.backend.community.post.domain.repository.PostDomainRepository;
import com.germogli.backend.common.exception.ResourceNotFoundException;
import com.germogli.backend.authentication.domain.model.UserDomain;
import com.germogli.backend.authentication.domain.repository.UserDomainRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de dominio para las publicaciones.
 * Contiene la lógica de negocio, incluyendo la validación de permisos para eliminar publicaciones.
 */
@RequiredArgsConstructor
@Service
public class PostDomainService {

    private final PostDomainRepository postRepository;
    // Repositorio del módulo de autenticación para obtener el usuario autenticado
    private final UserDomainRepository userRepository;

    public PostDomain createPost(CreatePostRequestDTO request) {
        // Extrae el usuario autenticado del contexto de seguridad
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();
        UserDomain currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado para el username: " + username));

        // Crea el objeto de dominio asignándole el userId y la fecha de creación
        PostDomain postDomain = PostDomain.builder()
                .userId(currentUser.getId())
                .postType(request.getPostType())
                .content(request.getContent())
                .multimediaContent(request.getMultimediaContent())
                .groupId(request.getGroupId())
                .threadId(request.getThreadId())
                .postDate(LocalDateTime.now())
                .build();

        return postRepository.save(postDomain);
    }

    public PostDomain getPostById(Integer id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post no encontrado con id: " + id));
    }

    public List<PostDomain> getAllPosts() {
        return postRepository.findAll();
    }

    /**
     * Actualiza una publicación.
     * Solo se permite actualizar si el usuario autenticado es el propietario del post.
     */
    public PostDomain updatePost(Integer id, UpdatePostRequestDTO request) {
        // Obtiene el usuario autenticado
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();
        UserDomain currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado para el username: " + username));

        // Recupera el post existente
        PostDomain existingPost = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post no encontrado con id: " + id));

        // Verifica que el usuario autenticado sea el dueño del post
        if (!existingPost.getUserId().equals(currentUser.getId())) {
            throw new AccessDeniedException("No tiene permisos para actualizar esta publicación");
        }

        // Actualiza los datos y la fecha de modificación
        existingPost.setPostType(request.getPostType());
        existingPost.setContent(request.getContent());
        existingPost.setMultimediaContent(request.getMultimediaContent());
        existingPost.setPostDate(LocalDateTime.now());
        return postRepository.save(existingPost);
    }

    /**
     * Elimina una publicación. La eliminación solo se permite si:
     * - El usuario autenticado es el propietario de la publicación, o
     * - El usuario tiene rol ADMINISTRADOR.
     */
    public void deletePost(Integer id) {
        // Obtener el usuario autenticado
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();
        UserDomain currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado para el username: " + username));
        // Obtener la publicación
        PostDomain post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post no encontrado con id: " + id));

        // Verificar permisos: solo el propietario o un administrador pueden eliminar
        boolean isOwner = post.getUserId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() != null &&
                currentUser.getRole().getRoleType().equalsIgnoreCase("ADMINISTRADOR");
        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("No tiene permisos para eliminar esta publicación");
        }
        postRepository.deleteById(id);
    }

    // Método de mapeo para convertir el dominio a DTO de respuesta
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

    // Métodos auxiliares para transformar una lista de dominios a DTOs
    public List<PostResponseDTO> toResponseList(List<PostDomain> posts) {
        return posts.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
