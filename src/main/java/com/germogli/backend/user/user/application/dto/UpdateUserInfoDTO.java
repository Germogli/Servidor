package com.germogli.backend.user.user.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para actualizar la información de un usuario.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserInfoDTO {
    private Integer userId;
    private String username;
    private String avatar;
    private String firstName;
    private String lastName;
    private String description;
}
