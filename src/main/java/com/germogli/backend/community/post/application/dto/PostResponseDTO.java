package com.germogli.backend.community.post.application.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para representar una publicación.
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
