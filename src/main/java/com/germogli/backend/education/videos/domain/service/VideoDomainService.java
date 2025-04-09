package com.germogli.backend.education.videos.domain.service;

import com.germogli.backend.authentication.domain.model.UserDomain;
import com.germogli.backend.common.exception.CustomForbiddenException;
import com.germogli.backend.common.exception.ResourceNotFoundException;
import com.germogli.backend.common.notification.application.service.NotificationService;
import com.germogli.backend.education.domain.service.EducationSharedService;
import com.germogli.backend.education.module.domain.service.ModuleDomainService;
import com.germogli.backend.education.videos.application.dto.CreateVideoRequestDTO;
import com.germogli.backend.education.videos.application.dto.UpdateVideoRequestDTO;
import com.germogli.backend.education.videos.application.dto.VideoResponseDTO;
import com.germogli.backend.education.videos.domain.model.VideoDomain;
import com.germogli.backend.education.videos.domain.repository.VideoDomainRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de dominio para gestionar videos educativos.
 */
@Service
@RequiredArgsConstructor
public class VideoDomainService {

    private final VideoDomainRepository videoDomainRepository;
    private final ModuleDomainService moduleDomainService;
    private final EducationSharedService educationSharedService;
    private final NotificationService notificationService;    // Servicio para enviar notificaciones a través de WebSockets

    /**
     * Crea un nuevo video educativo.
     *
     * @param dto Objeto CreateVideoRequestDTO con la información del video.
     * @return El objeto VideoDomain creado.
     */
    public VideoDomain createVideo(CreateVideoRequestDTO dto) {
        // Obtener el usuario autenticado
        UserDomain currentUser = educationSharedService.getAuthenticatedUser();
        if (!educationSharedService.hasRole(currentUser, "ADMINISTRADOR")) {
            throw new AccessDeniedException("El usuario no tiene permisos para crear videos.");
        }

        // Validar que el título no esté vacío
        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            throw new CustomForbiddenException("El título del video no puede estar vacío");
        }

        // Validar que la URL del video no esté vacía
        if (dto.getVideoUrl() == null || dto.getVideoUrl().trim().isEmpty()) {
            throw new CustomForbiddenException("La URL del video no puede estar vacía");
        }

        // Verificar que el módulo existe
        moduleDomainService.getModuleById(dto.getModuleId());

        // Crear el objeto VideoDomain
        VideoDomain videoDomain = VideoDomain.builder()
                .moduleId(com.germogli.backend.education.module.domain.model.ModuleDomain.builder()
                        .moduleId(dto.getModuleId())
                        .build())
                .title(dto.getTitle())
                .videoUrl(dto.getVideoUrl())
                .creationDate(LocalDateTime.now())
                .build();

        // Guardar el video en la base de datos
        VideoDomain createdVideo = videoDomainRepository.createVideo(videoDomain);

        // Obtener el nombre del módulo para la notificación
        String moduleName = moduleDomainService.getModuleById(dto.getModuleId()).getTitle();

        // Enviar notificación WebSocket después de crear el video
        notificationService.sendNotification(
                currentUser.getId(),
                "Se ha creado un nuevo video: " + createdVideo.getTitle() + " en el módulo " + moduleName,
                "education_video"
        );

        return createdVideo;
    }

    /**
     * Obtiene un video por su ID.
     *
     * @param id ID del video.
     * @return El objeto VideoDomain correspondiente.
     * @throws ResourceNotFoundException si no se encuentra.
     */
    public VideoDomain getVideoById(Integer id) {
        return videoDomainRepository.getById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Video no encontrado con id " + id));
    }

    /**
     * Obtiene todos los videos asociados a un módulo.
     *
     * @param moduleId ID del módulo.
     * @return Lista de VideoDomain.
     * @throws ResourceNotFoundException si no se encuentran videos.
     */
    public List<VideoDomain> getVideosByModuleId(Integer moduleId) {
        // Verificar que el módulo exista
        moduleDomainService.getModuleById(moduleId);
        List<VideoDomain> videos = videoDomainRepository.getVideosByModuleId(moduleId);
        if (videos.isEmpty()) {
            throw new ResourceNotFoundException("No hay videos disponibles para este módulo.");
        }
        return videos;
    }

    /**
     * Actualiza un video educativo.
     *
     * @param videoId ID del video a actualizar.
     * @param dto Objeto UpdateVideoRequestDTO con la nueva información.
     * @return El objeto VideoDomain actualizado.
     */
    public VideoDomain updateVideo(Integer videoId, UpdateVideoRequestDTO dto) {
        // Asignar el ID al DTO (si es necesario)
        dto.setVideoId(videoId);

        // Obtener el usuario autenticado y verificar permisos
        UserDomain currentUser = educationSharedService.getAuthenticatedUser();
        if (!educationSharedService.hasRole(currentUser, "ADMINISTRADOR")) {
            throw new AccessDeniedException("El usuario no tiene permisos para actualizar videos.");
        }

        // Verificar que el video exista
        VideoDomain existingVideo = videoDomainRepository.getById(videoId)
                .orElseThrow(() -> new ResourceNotFoundException("Video no encontrado con id " + videoId));

        // Validar que el título no esté vacío
        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            throw new CustomForbiddenException("El título del video no puede estar vacío");
        }

        // Validar que la URL del video no esté vacía
        if (dto.getVideoUrl() == null || dto.getVideoUrl().trim().isEmpty()) {
            throw new CustomForbiddenException("La URL del video no puede estar vacía");
        }

        // Verificar que el módulo exista, si se envía
        if (dto.getModuleId() != null) {
            moduleDomainService.getModuleById(dto.getModuleId());
        }

        // Convertir el DTO a VideoDomain de forma explícita
        VideoDomain videoDomain = VideoDomain.builder()
                .videoId(videoId)
                .moduleId(com.germogli.backend.education.module.domain.model.ModuleDomain.builder()
                        .moduleId(dto.getModuleId())
                        .build())
                .title(dto.getTitle())
                .videoUrl(dto.getVideoUrl())
                .build();

        // Llamar al repositorio para actualizar mediante el SP
        videoDomainRepository.updateVideo(videoDomain);

        // Obtener el nombre del módulo para la notificación
        String moduleName = "";
        if (dto.getModuleId() != null) {
            moduleName = moduleDomainService.getModuleById(dto.getModuleId()).getTitle();
        }
        String moduloInfo = !moduleName.isEmpty() ? " en el módulo " + moduleName : "";

        // Enviar notificación WebSocket después de actualizar el video
        notificationService.sendNotification(
                currentUser.getId(),
                "Se ha actualizado el video: " + dto.getTitle() + moduloInfo,
                "education_video"
        );

        // Recuperar el video actualizado para obtener todos los campos (si es necesario)
        return videoDomainRepository.getById(videoId)
                .orElseThrow(() -> new ResourceNotFoundException("Error al recuperar el video actualizado con id " + videoId));
    }

    /**
     * Elimina un video educativo de la base de datos.
     *
     * @param videoId ID del video a eliminar.
     * @throws ResourceNotFoundException si no se encuentra.
     * @throws AccessDeniedException si el usuario no tiene permisos.
     */
    public void deleteVideo(Integer videoId) {
        UserDomain currentUser = educationSharedService.getAuthenticatedUser();
        if (!educationSharedService.hasRole(currentUser, "ADMINISTRADOR")) {
            throw new AccessDeniedException("El usuario no tiene permisos para eliminar videos.");
        }

        // Verificar que el video existe y obtenerlo para la notificación
        VideoDomain video = videoDomainRepository.getById(videoId)
                .orElseThrow(() -> new ResourceNotFoundException("Video no encontrado con id " + videoId));

        // Obtener el nombre del módulo para la notificación (si el módulo existe)
        String moduleName = "";
        if (video.getModuleId() != null && video.getModuleId().getModuleId() != null) {
            try {
                moduleName = moduleDomainService.getModuleById(video.getModuleId().getModuleId()).getTitle();
            } catch (ResourceNotFoundException e) {
                // Si el módulo ya no existe, se maneja silenciosamente
            }
        }
        String moduloInfo = !moduleName.isEmpty() ? " del módulo " + moduleName : "";

        // Enviar notificación WebSocket antes de eliminar el video
        notificationService.sendNotification(
                currentUser.getId(),
                "Se ha eliminado el video: " + video.getTitle() + moduloInfo,
                "education_video"
        );

        // Eliminar el video de la base de datos
        videoDomainRepository.deleteVideo(videoId);
    }

    /**
     * Convierte una lista de objetos de dominio VideoDomain a DTOs de respuesta.
     *
     * @param domains Lista de objetos VideoDomain que representan los videos en la capa de dominio.
     * @return Lista de objetos VideoResponseDTO con los datos formateados para la respuesta al cliente.
     */
    public List<VideoResponseDTO> toResponseList(List<VideoDomain> domains) {
        return domains.stream()
                .map(domain -> {
                    VideoResponseDTO dto = new VideoResponseDTO();
                    dto.setVideoId(domain.getVideoId());
                    dto.setTitle(domain.getTitle());
                    dto.setVideoUrl(domain.getVideoUrl());
                    dto.setCreationDate(domain.getCreationDate());
                    dto.setModuleId(domain.getModuleId() != null ? domain.getModuleId().getModuleId() : null);
                    return dto;
                })
                .collect(Collectors.toList());
    }

}
