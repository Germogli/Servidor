package com.germogli.backend.common.notification;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Servicio encargado de publicar notificaciones a través de WebSockets.
 * Utiliza SimpMessagingTemplate para enviar mensajes a destinos específicos.
 */
@Service
public class NotificationPublisher {

    // Inyección del template de mensajería para enviar notificaciones vía WebSocket.
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Constructor para inyectar el SimpMessagingTemplate.
     *
     * @param messagingTemplate Plantilla de mensajería de Spring para WebSocket.
     */
    public NotificationPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Publica una notificación vía WebSocket.
     *
     * @param userId   El ID del usuario destinatario (opcional, se puede usar para topics personalizados).
     * @param message  El mensaje de la notificación.
     * @param category La categoría de la notificación (por ejemplo, "post", "thread").
     */
    public void publishNotification(Integer userId, String message, String category) {
        // Define un destino general; se puede personalizar para cada usuario, por ejemplo: "/topic/notifications/" + userId
        String destination = "/topic/notifications";
        // Crea un payload con la información de la notificación
        NotificationPayload payload = new NotificationPayload(userId, message, category);
        // Envía el payload al destino definido
        messagingTemplate.convertAndSend(destination, payload);
    }

    /**
     * Clase interna para representar la carga útil (payload) de una notificación.
     */
    public static class NotificationPayload {
        private Integer userId;
        private String message;
        private String category;

        /**
         * Constructor para inicializar el payload de la notificación.
         *
         * @param userId   El ID del usuario destinatario.
         * @param message  El mensaje de la notificación.
         * @param category La categoría de la notificación.
         */
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
