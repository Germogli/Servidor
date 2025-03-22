package com.germogli.backend.community.notification.web.controller;

import com.germogli.backend.community.application.dto.common.ApiResponseDTO;
import com.germogli.backend.community.notification.application.dto.CreateNotificationRequestDTO;
import com.germogli.backend.community.notification.application.dto.NotificationResponseDTO;
import com.germogli.backend.community.notification.domain.model.NotificationDomain;
import com.germogli.backend.community.notification.domain.service.NotificationDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

/**
 * Controlador REST para la gestión de notificaciones en Community.
 * Proporciona endpoints para crear, obtener y eliminar notificaciones.
 */
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationDomainService notificationService;

    /**
     * Endpoint para crear una nueva notificación.
     *
     * @param request DTO con los datos para crear la notificación.
     * @return Respuesta API con la notificación creada.
     */
    @PostMapping
    public ResponseEntity<ApiResponseDTO<NotificationResponseDTO>> createNotification(@Valid @RequestBody CreateNotificationRequestDTO request) {
        NotificationDomain notification = notificationService.createNotification(request);
        return ResponseEntity.ok(ApiResponseDTO.<NotificationResponseDTO>builder()
                .message("Notificación creada correctamente")
                .data(notificationService.toNotificationResponse(notification))
                .build());
    }

    /**
     * Endpoint para obtener las notificaciones de un usuario.
     *
     * @param userId Identificador del usuario.
     * @return Respuesta API con la lista de notificaciones.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponseDTO<List<NotificationResponseDTO>>> getNotificationsByUser(@PathVariable Integer userId) {
        List<NotificationDomain> notifications = notificationService.getNotificationsByUser(userId);
        return ResponseEntity.ok(ApiResponseDTO.<List<NotificationResponseDTO>>builder()
                .message("Notificaciones recuperadas correctamente")
                .data(notificationService.toNotificationResponseList(notifications))
                .build());
    }

    /**
     * Endpoint para eliminar una notificación por su ID.
     *
     * @param id Identificador de la notificación.
     * @return Respuesta API confirmando la eliminación.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteNotification(@PathVariable Integer id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.ok(ApiResponseDTO.<Void>builder()
                .message("Notificación eliminada correctamente")
                .build());
    }
}
