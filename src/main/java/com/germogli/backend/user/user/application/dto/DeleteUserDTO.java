package com.germogli.backend.user.user.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Anotaciones lombok para generar codigo repetitivo al correr la aplicacion
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeleteUserDTO {
    private Integer userId;
}
