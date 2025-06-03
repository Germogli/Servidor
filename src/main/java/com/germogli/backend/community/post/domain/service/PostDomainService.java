package com.germogli.backend.community.post.domain.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.germogli.backend.authentication.domain.model.UserDomain;
import com.germogli.backend.common.azure.AzureBlobStorageService;
import com.germogli.backend.common.exception.CustomForbiddenException;
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
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
        String multimediaUrl = request.getMultimediaContent();

        // Procesar el archivo si se envía
        MultipartFile file = request.getFile();
        if (file != null && !file.isEmpty()) {
            try {
                // Verificar si es una imagen o un video
                String contentType = file.getContentType();
                if (contentType != null) {
                    long fileSizeInMB = file.getSize() / (1024 * 1024);

                    // Validación para imágenes
                    if (contentType.startsWith("image/")) {
                        if (fileSizeInMB > 10) {
                            throw new CustomForbiddenException("La imagen excede el límite de 10MB. Su tamaño actual es de "
                                    + fileSizeInMB + "MB.");
                        }
                    }
                    // Validación para videos
                    else if (contentType.startsWith("video/")) {
                        if (fileSizeInMB > 1024) {
                            throw new CustomForbiddenException("El video excede el límite de 1GB. Su tamaño actual es de "
                                    + fileSizeInMB + "MB.");
                        }

                        // Para archivos grandes (videos), usar método de carga por bloques
                        if (fileSizeInMB > 100) {
                            String sanitizedFileName = sanitizeFileName(file.getOriginalFilename());
                            String fileName = currentUser.getId() + "" + System.currentTimeMillis() + "" + sanitizedFileName;
                            multimediaUrl = uploadLargeFileToAzure("publicaciones", fileName, file);
                            return finalizePostCreation(currentUser, request, multimediaUrl);
                        }
                    }
                }

                // Para archivos pequeños, continuar con el método original
                String sanitizedFileName = sanitizeFileName(file.getOriginalFilename());
                String fileName = currentUser.getId() + "" + System.currentTimeMillis() + "" + sanitizedFileName;

                // Subir archivo
                azureBlobStorageService.uploadFile("publicaciones", fileName, file.getInputStream(), file.getSize());

                // Obtener URL del archivo - Agregar verificación
                String blobUrl = azureBlobStorageService.getBlobUrl("publicaciones", fileName);

                // Si la URL está vacía, intentar construirla manualmente o verificar el blob
                if (blobUrl == null || blobUrl.trim().isEmpty()) {
                    // Log para debugging
                    System.err.println("ADVERTENCIA: getBlobUrl retornó vacío para: " + fileName);

                    // Verificar si el blob existe y obtener la URL con SAS token
                    multimediaUrl = azureBlobStorageService.generateSasToken("publicaciones", fileName, 360);

                    if (multimediaUrl == null || multimediaUrl.trim().isEmpty()) {
                        throw new RuntimeException("No se pudo obtener la URL del archivo subido");
                    }
                } else {
                    multimediaUrl = blobUrl;
                }

            } catch (IOException e) {
                throw new RuntimeException("Error al subir el archivo a Azure Blob Storage", e);
            }
        }

        return finalizePostCreation(currentUser, request, multimediaUrl);
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
    public PostDomain updatePost(Integer id, UpdatePostRequestDTO request, MultipartFile file) {
        UserDomain currentUser = sharedService.getAuthenticatedUser();
        PostDomain existingPost = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post no encontrado con id: " + id));

        boolean isOwner = existingPost.getUserId().equals(currentUser.getId());
        boolean isAdmin = sharedService.hasRole(currentUser, "ADMINISTRADOR");
        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("No tiene permisos para actualizar esta publicación.");
        }

        // CAMBIO PRINCIPAL: Inicializar con el contenido multimedia existente
        String multimediaUrl = existingPost.getMultimediaContent();

        // Si hay un archivo nuevo, procesarlo
        if (file != null && !file.isEmpty()) {
            try {
                // Eliminar el archivo anterior si existe
                if (existingPost.getMultimediaContent() != null && !existingPost.getMultimediaContent().trim().isEmpty()) {
                    String oldBlobName = extractBlobNameFromUrl(existingPost.getMultimediaContent());
                    azureBlobStorageService.deleteBlob("publicaciones", oldBlobName);
                }

                // Verificar si es una imagen o un video
                String contentType = file.getContentType();
                if (contentType != null) {
                    long fileSizeInMB = file.getSize() / (1024 * 1024);

                    // Validación para imágenes
                    if (contentType.startsWith("image/")) {
                        if (fileSizeInMB > 10) {
                            throw new CustomForbiddenException("La imagen excede el límite de 10MB. Su tamaño actual es de "
                                    + fileSizeInMB + "MB.");
                        }
                    }
                    // Validación para videos
                    else if (contentType.startsWith("video/")) {
                        if (fileSizeInMB > 1024) {
                            throw new CustomForbiddenException("El video excede el límite de 1GB. Su tamaño actual es de "
                                    + fileSizeInMB + "MB.");
                        }

                        // Para archivos grandes (videos), usar método de carga por bloques
                        if (fileSizeInMB > 100) {
                            String sanitizedFileName = sanitizeFileName(file.getOriginalFilename());
                            String fileName = currentUser.getId() + "" + System.currentTimeMillis() + "" + sanitizedFileName;
                            multimediaUrl = uploadLargeFileToAzure("publicaciones", fileName, file);
                            return finalizePostUpdate(existingPost, request, multimediaUrl);
                        }
                    }
                }

                // Para archivos pequeños, continuar con el método original
                String sanitizedFileName = sanitizeFileName(file.getOriginalFilename());
                String fileName = currentUser.getId() + "" + System.currentTimeMillis() + "" + sanitizedFileName;

                // Subir archivo
                azureBlobStorageService.uploadFile("publicaciones", fileName, file.getInputStream(), file.getSize());

                // Obtener URL del archivo con verificación
                String blobUrl = azureBlobStorageService.getBlobUrl("publicaciones", fileName);

                if (blobUrl == null || blobUrl.trim().isEmpty()) {
                    System.err.println("ADVERTENCIA: getBlobUrl retornó vacío para: " + fileName);
                    multimediaUrl = azureBlobStorageService.generateSasToken("publicaciones", fileName, 360);

                    if (multimediaUrl == null || multimediaUrl.trim().isEmpty()) {
                        throw new RuntimeException("No se pudo obtener la URL del archivo subido");
                    }
                } else {
                    multimediaUrl = blobUrl;
                }

            } catch (IOException e) {
                throw new RuntimeException("Error al subir el archivo a Azure Blob Storage", e);
            }
        }
        // CAMBIO PRINCIPAL: Solo eliminar multimedia si se envía explícitamente una cadena vacía
        else if (request.getMultimediaContent() != null &&
                request.getMultimediaContent().trim().isEmpty() &&
                existingPost.getMultimediaContent() != null) {
            // El usuario explícitamente quiere eliminar el archivo multimedia
            String oldBlobName = extractBlobNameFromUrl(existingPost.getMultimediaContent());
            azureBlobStorageService.deleteBlob("publicaciones", oldBlobName);
            multimediaUrl = null;
        }
        // CAMBIO PRINCIPAL: Si request.getMultimediaContent() es null, mantener el existente
        // (esto significa que no se está actualizando el campo multimedia)

        return finalizePostUpdate(existingPost, request, multimediaUrl);
    }

    // Método auxiliar para finalizar la actualización del post
    private PostDomain finalizePostUpdate(PostDomain existingPost, UpdatePostRequestDTO request, String multimediaUrl) {
        existingPost.setPostType(request.getPostType());
        existingPost.setContent(request.getContent());
        existingPost.setMultimediaContent(multimediaUrl);
        existingPost.setPostDate(LocalDateTime.now());

        PostDomain updatedPost = postRepository.save(existingPost);

        // Notificaciones
        UserDomain currentUser = sharedService.getAuthenticatedUser();
        if (existingPost.getUserId().equals(currentUser.getId())) {
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

        try {
            // Extraer la parte después del último '/'
            int lastSlashIndex = url.lastIndexOf('/');
            if (lastSlashIndex == -1) {
                return "";
            }

            String fileName = url.substring(lastSlashIndex + 1);

            // Eliminar cualquier parámetro de consulta (después de ?)
            int queryIndex = fileName.indexOf('?');
            if (queryIndex != -1) {
                fileName = fileName.substring(0, queryIndex);
            }

            // Importante: NO decodificar el nombre del archivo aquí
            // Simplemente devolver el nombre del archivo tal como está en la URL
            return fileName;
        } catch (Exception e) {
            System.err.println("Error al extraer nombre de archivo: " + e.getMessage());
            return "";
        }
    }

    /**
     * Convierte un objeto PostDomain en un DTO de respuesta.
     *
     * @param post Publicación a convertir.
     * @return DTO con la información del post.
     */
    public PostResponseDTO toResponse(PostDomain post) {
        String secureUrl = post.getMultimediaContent();

        // Generar URL segura con SAS token si hay contenido multimedia
        if (secureUrl != null && !secureUrl.isEmpty() &&
                secureUrl.contains("germoglistorage.blob.core.windows.net/publicaciones")) {
            // Extraer el nombre del archivo desde la URL
            String fileName = extractBlobNameFromUrl(secureUrl);
            // Generar token SAS con duración de 6 horas
            secureUrl = azureBlobStorageService.generateSasToken(
                    "publicaciones",
                    fileName,
                    360  // 6 horas de duración
            );
        }

        return PostResponseDTO.builder()
                .id(post.getId())
                .userId(post.getUserId())
                .postType(post.getPostType())
                .content(post.getContent())
                .multimediaContent(secureUrl) // URL con token SAS
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

    // Método auxiliar para finalizar la creación del post
    private PostDomain finalizePostCreation(UserDomain currentUser, CreatePostRequestDTO request, String multimediaUrl) {
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

    // Método para subir archivos grandes usando técnica de bloques
    private String uploadLargeFileToAzure(String containerName, String blobName, MultipartFile file) throws IOException {
        // Obtener el contenedor
        BlobContainerClient containerClient = azureBlobStorageService.getOrCreateContainer(containerName);
        BlobClient blobClient = containerClient.getBlobClient(blobName);
        BlockBlobClient blockBlobClient = blobClient.getBlockBlobClient();

        // Subir por bloques
        InputStream inputStream = file.getInputStream();
        int blockSize = 4 * 1024 * 1024; // 4MB por bloque
        byte[] buffer = new byte[blockSize];
        List<String> blockIds = new ArrayList<>();
        int blockIndex = 0;
        int bytesRead;

        while ((bytesRead = inputStream.read(buffer)) != -1) {
            String blockId = Base64.getEncoder().encodeToString(
                    String.format("%06d", blockIndex).getBytes(StandardCharsets.UTF_8));
            blockIds.add(blockId);

            // Subir el bloque
            if (bytesRead < buffer.length) {
                byte[] trimmedBuffer = Arrays.copyOf(buffer, bytesRead);
                blockBlobClient.stageBlock(blockId, new ByteArrayInputStream(trimmedBuffer), bytesRead);
            } else {
                blockBlobClient.stageBlock(blockId, new ByteArrayInputStream(buffer), bytesRead);
            }

            blockIndex++;
        }

        // Finalizar la subida
        blockBlobClient.commitBlockList(blockIds);
        return blobClient.getBlobUrl();
    }
    /**
     * Obtiene todas las publicaciones que pertenecen a un grupo específico.
     *
     * @param groupId ID del grupo
     * @return Lista de publicaciones del grupo
     * @throws ResourceNotFoundException si no existe el grupo
     */
    public List<PostDomain> getPostsByGroupId(Integer groupId) {
        // Utiliza el servicio compartido para validar la existencia del grupo
        sharedService.validateGroupExists(groupId);

        List<PostDomain> posts = postRepository.findByGroupId(groupId);
        if (posts.isEmpty()) {
            return Collections.emptyList(); // Devuelve lista vacía en lugar de error
        }
        return posts;
    }
    /**
     * Obtiene todas las publicaciones creadas por un usuario específico.
     * Si no se proporciona ID, usa el usuario autenticado actual.
     *
     * @param userId ID del usuario o null para usar el usuario autenticado
     * @return Lista de publicaciones del usuario
     */
    public List<PostDomain> getPostsByUserId(Integer userId) {
        // Si no se proporciona ID, usar el usuario autenticado
        Integer targetUserId = userId;
        if (targetUserId == null) {
            UserDomain currentUser = sharedService.getAuthenticatedUser();
            targetUserId = currentUser.getId();
        }

        return postRepository.findByUserId(targetUserId);
    }

    private String sanitizeFileName(String originalFileName) {
        if (originalFileName == null || originalFileName.isEmpty()) {
            return "file_" + System.currentTimeMillis();
        }

        // Separar nombre y extensión
        String extension = "";
        int lastDotIndex = originalFileName.lastIndexOf('.');
        String baseName = originalFileName;

        if (lastDotIndex > 0) {
            extension = originalFileName.substring(lastDotIndex);
            baseName = originalFileName.substring(0, lastDotIndex);
        }

        // Reemplazar espacios y caracteres especiales
        baseName = baseName
                .replaceAll("\\s+", "_")           // Reemplazar espacios por guiones bajos
                .replaceAll("[^a-zA-Z0-9._-]", "") // Eliminar caracteres especiales excepto . _ -
                .replaceAll("_{2,}", "_");         // Reemplazar múltiples guiones bajos por uno solo

        // Si el nombre queda vacío después de la limpieza
        if (baseName.isEmpty()) {
            baseName = "file";
        }

        return baseName + extension;
    }
}
