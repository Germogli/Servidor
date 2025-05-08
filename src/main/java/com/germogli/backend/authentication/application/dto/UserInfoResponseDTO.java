package com.germogli.backend.authentication.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO que contiene información básica del usuario para devolver tras autenticación.
 * No incluye información sensible como contraseñas o tokens.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResponseDTO {
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
}