package com.germogli.backend.authentication.domain.model;

import lombok.*;

/**
 * Representa un rol en el dominio.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role {
    private Integer id;
    private String roleType;
}
