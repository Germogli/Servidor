package com.germogli.backend.community.reaction.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la creación de una reacción.
 * Contiene los datos necesarios para agregar una reacción a un post.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateReactionRequestDTO {
    @NotNull(message = "El ID del post es obligatorio")
    private Integer postId;

    @NotBlank(message = "El tipo de reacción es obligatorio")
    private String reactionType;
}
