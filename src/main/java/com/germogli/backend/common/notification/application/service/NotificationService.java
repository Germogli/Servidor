package com.germogli.backend.common.notification.application.service;

import com.germogli.backend.common.exception.NotificationException;
import com.germogli.backend.common.notification.domain.model.NotificationDomain;
import com.germogli.backend.common.notification.infrastructure.messaging.NotificationPublisher;
import com.germogli.backend.common.notification.infrastructure.repository.NotificationRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public NotificationService(NotificationRepository notificationRepository,
                               SimpMessagingTemplate messagingTemplate) {
        this.notificationRepository = notificationRepository;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Crea y persiste una notificación, y la envía en tiempo real vía WebSocket.
     *
     * @param userId   ID del usuario destinatario (opcional).
     * @param message  Mensaje de la notificación.
     * @param category Categoría de la notificación (ej. "post", "thread").
     * @return Notificación de dominio creada.
     */
    @Transactional
    public NotificationDomain sendNotification(Integer userId, String message, String category) {
        try {
            // Construir la notificación de dominio
            NotificationDomain notification = NotificationDomain.builder()
                    .userId(userId)
                    .message(message)
                    .category(category)
                    .notificationDate(LocalDateTime.now())
                    .isRead(false)
                    .build();

            // Persistir la notificación
            NotificationDomain saved = NotificationDomain.fromEntity(
                    notificationRepository.save(notification.toEntity())
            );

            // Enviar la notificación en tiempo real vía WebSocket
            String destination = "/topic/notifications";
            NotificationPublisher.NotificationPayload payload =
                    new NotificationPublisher.NotificationPayload(userId, message, category);
            messagingTemplate.convertAndSend(destination, payload);

            return saved;
        } catch (Exception e) {
            // Se captura cualquier excepción y se envuelve en una NotificationException
            throw new NotificationException("Error al enviar la notificación", e);
        }
    }
}
