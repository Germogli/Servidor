package com.germogli.backend.common.notification.infrastructure.messaging;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Componente de infraestructura encargado de enviar notificaciones vía WebSocket.
 */
@Component
public class NotificationPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public NotificationPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Envía una notificación a través del WebSocket.
     *
     * @param userId   ID del usuario destinatario (opcional, se puede personalizar el topic).
     * @param message  Mensaje de la notificación.
     * @param category Categoría de la notificación (ejemplo: "post", "thread").
     */
    public void publishNotification(Integer userId, String message, String category) {
        String destination = "/topic/notifications";
        NotificationPayload payload = new NotificationPayload(userId, message, category);
        messagingTemplate.convertAndSend(destination, payload);
    }

    /**
     * Clase que representa la carga útil (payload) de una notificación.
     */
    public static class NotificationPayload {
        private final Integer userId;
        private final String message;
        private final String category;

        public NotificationPayload(Integer userId, String message, String category) {
            this.userId = userId;
            this.message = message;
            this.category = category;
        }

        public Integer getUserId() { return userId; }
        public String getMessage() { return message; }
        public String getCategory() { return category; }
    }
}
