package com.germogli.backend.user.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Anotaciones lombok para generar codigo repetitivo al correr la aplicacion
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
