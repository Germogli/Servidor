package com.germogli.backend.user.user.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para mostrar información básica de usuarios en listados.
 * Incluye información esencial sin datos sensibles como contraseñas.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {
    private Integer id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String country;
    private String avatar;
    private String description;
    private Boolean isActive;
    private String roleType;
    private LocalDateTime creationDate;
}