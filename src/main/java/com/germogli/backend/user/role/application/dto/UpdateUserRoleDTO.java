package com.germogli.backend.user.role.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Anotaciones lombok para generar codigo repetitivo al correr la aplicacion
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRoleDTO {
    private Integer userId;
    private Integer roleId;
}
