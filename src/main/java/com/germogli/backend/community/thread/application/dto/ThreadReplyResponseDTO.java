package com.germogli.backend.community.thread.application.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/**
 * DTO de respuesta para una respuesta a un hilo.
 */
@Data
@Builder
public class ThreadReplyResponseDTO {
    private Integer id;
    private Integer threadId;
    private Integer userId;
    private String content;
    private LocalDateTime creationDate;
}
