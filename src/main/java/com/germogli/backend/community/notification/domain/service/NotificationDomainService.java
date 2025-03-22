package com.germogli.backend.community.notification.domain.service;

import com.germogli.backend.community.notification.application.dto.NotificationResponseDTO;
import com.germogli.backend.community.notification.domain.model.NotificationDomain;
import com.germogli.backend.community.notification.domain.repository.NotificationDomainRepository;
import com.germogli.backend.community.notification.application.dto.CreateNotificationRequestDTO;
import com.germogli.backend.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de dominio para la gestión de notificaciones.
 * Contiene la lógica de negocio para crear, obtener, listar y eliminar notificaciones.
 */
@Service
@RequiredArgsConstructor
public class NotificationDomainService {

    private final NotificationDomainRepository notificationRepository;

    /**
     * Crea una nueva notificación.
     *
     * @param request DTO con los datos para crear la notificación.
     * @return Notificación creada.
     */
    public NotificationDomain createNotification(CreateNotificationRequestDTO request) {
        NotificationDomain notification = NotificationDomain.builder()
                .userId(request.getUserId())
                .message(request.getMessage())
                .category(request.getCategory())
                .build();
        return notificationRepository.save(notification);
    }

    /**
     * Obtiene las notificaciones de un usuario.
     *
     * @param userId Identificador del usuario.
     * @return Lista de notificaciones.
     * @throws ResourceNotFoundException si no se encuentran notificaciones.
     */
    public List<NotificationDomain> getNotificationsByUser(Integer userId) {
        List<NotificationDomain> notifications = notificationRepository.findByUserId(userId);
        if (notifications.isEmpty()) {
            throw new ResourceNotFoundException("No se encontraron notificaciones para el usuario con id: " + userId);
        }
        return notifications;
    }

    /**
     * Elimina una notificación por su ID.
     *
     * @param id Identificador de la notificación a eliminar.
     */
    public void deleteNotification(Integer id) {
        notificationRepository.deleteById(id);
    }

    /**
     * Convierte un objeto NotificationDomain en un DTO de respuesta.
     *
     * @param notification Notificación a convertir.
     * @return DTO con la información de la notificación.
     */
    public NotificationResponseDTO toNotificationResponse(NotificationDomain notification) {
        return NotificationResponseDTO.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .message(notification.getMessage())
                .category(notification.getCategory())
                .creationDate(notification.getCreationDate())
                .build();
    }

    /**
     * Convierte una lista de NotificationDomain en una lista de DTOs de respuesta.
     *
     * @param notifications Lista de notificaciones.
     * @return Lista de DTOs.
     */
    public List<NotificationResponseDTO> toNotificationResponseList(List<NotificationDomain> notifications) {
        return notifications.stream().map(this::toNotificationResponse).collect(Collectors.toList());
    }
}
