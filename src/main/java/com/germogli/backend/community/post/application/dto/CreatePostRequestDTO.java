package com.germogli.backend.community.post.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePostRequestDTO {
    @NotBlank(message = "El tipo de post es obligatorio")
    private String postType;

    @NotBlank(message = "El contenido es obligatorio")
    private String content;

    // Opcional: URL del contenido multimedia
    private String multimediaContent;

    // Opcionales: Si el post pertenece a un grupo o hilo
    private Integer groupId;
    private Integer threadId;
}

