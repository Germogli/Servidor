package com.germogli.backend.community.thread.application.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/**
 * DTO de respuesta para un hilo.
 */
@Data
@Builder
public class ThreadResponseDTO {
    private Integer id;
    private Integer groupId;
    private Integer userId;
    private String title;
    private String content;
    private LocalDateTime creationDate;
}
