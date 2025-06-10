package com.germogli.backend.user.user.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para solicitar un usuario por su username.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetUserByUsernameDTO {
    private String username;
}
