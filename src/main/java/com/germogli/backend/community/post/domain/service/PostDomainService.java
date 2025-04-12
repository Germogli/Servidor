package com.germogli.backend.community.post.domain.service;

import com.germogli.backend.authentication.domain.model.UserDomain;
import com.germogli.backend.common.azure.AzureBlobStorageService;
import com.germogli.backend.common.exception.ResourceNotFoundException;
import com.germogli.backend.common.notification.application.service.NotificationService;
import com.germogli.backend.community.post.domain.model.PostDomain;
import com.germogli.backend.community.post.domain.repository.PostDomainRepository;
import com.germogli.backend.community.post.application.dto.CreatePostRequestDTO;
import com.germogli.backend.community.post.application.dto.PostResponseDTO;
import com.germogli.backend.community.post.application.dto.UpdatePostRequestDTO;
import com.germogli.backend.community.domain.service.CommunitySharedService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    private final AzureBlobStorageService azureBlobStorageService;

    /**
     * Crea una nueva publicación.
     * Si se adjunta un archivo, se sube a Azure Blob Storage en el contenedor "publicaciones"
     * y se almacena la URL obtenida en multimediaContent.
     *
     * @param request DTO con los datos para crear el post.
     * @return Publicación creada.
     */
    @Transactional
    public PostDomain createPost(CreatePostRequestDTO request) {
        UserDomain currentUser = sharedService.getAuthenticatedUser();
        // Valor inicial: se toma el valor que venga en el DTO (o vacío)
        String multimediaUrl = request.getMultimediaContent();

        // Procesar el archivo si se envía
        MultipartFile file = request.getFile();
        if (file != null && !file.isEmpty()) {
            try {
                // Generar un nombre único para el archivo (por ejemplo: "userId_timestamp_nombreOriginal")
                String fileName = currentUser.getId() + "_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
                // Subir el archivo al contenedor "publicaciones"
                azureBlobStorageService.uploadFile("publicaciones", fileName, file.getInputStream(), file.getSize());
                // Recuperar la URL permanente del blob (no se usa SAS token para este caso)
                multimediaUrl = azureBlobStorageService.getBlobUrl("publicaciones", fileName);
            } catch (IOException e) {
                throw new RuntimeException("Error al subir el archivo a Azure Blob Storage", e);
            }
        }

        PostDomain post = PostDomain.builder()
                .userId(currentUser.getId())
                .postType(request.getPostType())
                .content(request.getContent())
                .multimediaContent(multimediaUrl)
                .groupId(request.getGroupId())
                .threadId(request.getThreadId())
                .postDate(LocalDateTime.now())
                .build();

        return postRepository.save(post);
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
     * Se actualizan únicamente los campos textuales; la actualización de multimedia se asume que se hace en otra operación.
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
        // Se actualiza el campo multimediaContent según lo enviado en el DTO (sin archivo)
        existingPost.setMultimediaContent(request.getMultimediaContent());
        existingPost.setPostDate(LocalDateTime.now());

        PostDomain updatedPost = postRepository.save(existingPost);

        if (isOwner) {
            notificationService.sendNotification(
                    currentUser.getId(),
                    "Se ha actualizado tu publicación.",
                    "post"
            );
        } else {
            notificationService.sendNotification(
                    existingPost.getUserId(),
                    "Tu publicación ha sido actualizada por un administrador.",
                    "post"
            );
        }

        return updatedPost;
    }

    /**
     * Elimina una publicación.
     * Solo el propietario o un administrador pueden eliminar el post.
     * Además, si la publicación tiene contenido multimedia, se elimina el archivo del contenedor "publicaciones" en Azure Blob Storage.
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

        // Si hay contenido multimedia, eliminar el archivo del contenedor "publicaciones"
        if (post.getMultimediaContent() != null && !post.getMultimediaContent().trim().isEmpty()) {
            String blobName = extractBlobNameFromUrl(post.getMultimediaContent());
            azureBlobStorageService.deleteBlob("publicaciones", blobName);
        }

        postRepository.deleteById(id);
        notificationService.sendNotification(
                post.getUserId(),
                "Tu publicación ha sido eliminada.",
                "post"
        );
    }

    /**
     * Método auxiliar para extraer el nombre del blob a partir de la URL.
     * Se asume que la URL tiene el formato:
     * https://<account>.blob.core.windows.net/publicaciones/<blobName>?...
     *
     * @param url URL del blob.
     * @return Nombre del blob.
     */
    private String extractBlobNameFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return "";
        }
        int lastSlash = url.lastIndexOf('/');
        int queryIndex = url.indexOf('?', lastSlash);
        return (queryIndex > lastSlash) ? url.substring(lastSlash + 1, queryIndex) : url.substring(lastSlash + 1);
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
