package com.germogli.backend.community.message.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * DTO para transferencia de mensajes vía WebSocket.
 * Contiene la información necesaria para enviar y recibir mensajes en tiempo real.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageWebSocketDTO {
    private Integer id;
    private Integer userId;
    private String username;
    private String userAvatar;
    private String content;

    // Campos para determinar el contexto del mensaje
    private Integer postId;
    private Integer threadId;
    private Integer groupId;

    private LocalDateTime timestamp;
    private String messageType;
}