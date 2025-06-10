package com.germogli.backend.user.user.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la eliminación de un usuario.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeleteUserDTO {
    private Integer userId;
}
