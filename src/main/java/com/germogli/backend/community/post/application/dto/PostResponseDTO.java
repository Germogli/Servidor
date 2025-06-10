package com.germogli.backend.community.post.application.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/**
 * DTO de respuesta para una publicación.
 */
@Data
@Builder
public class PostResponseDTO {
    private Integer id;
    private Integer userId;
    private String postType;
    private String content;
    private String multimediaContent;
    private LocalDateTime postDate;
    private Integer groupId;
    private Integer threadId;
}
