package com.germogli.backend.user.user.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Modelo de dominio para representar un usuario.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Integer id;
    private String username;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String country;
    private String avatar;
    private String description;
    private Boolean isActive;
    private Role role;
    private LocalDateTime creationDate;
}
