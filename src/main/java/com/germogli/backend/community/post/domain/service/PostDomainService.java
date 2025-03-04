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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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

        // Crea el objeto de dominio asignándole el userId obtenido y la fecha de creación
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

    public PostDomain updatePost(Integer id, UpdatePostRequestDTO request) {
        PostDomain existingPost = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post no encontrado con id: " + id));

        existingPost.setPostType(request.getPostType());
        existingPost.setContent(request.getContent());
        existingPost.setMultimediaContent(request.getMultimediaContent());
        // Se actualiza la fecha de modificación, si se desea
        existingPost.setPostDate(LocalDateTime.now());
        return postRepository.save(existingPost);
    }

    public void deletePost(Integer id) {
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

    // Métodos auxiliares para la respuesta
    public List<PostResponseDTO> toResponseList(List<PostDomain> posts) {
        return posts.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}