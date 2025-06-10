package com.germogli.backend.authentication.infrastructure.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Mapea la tabla 'roles' en la base de datos.
 * En este caso la tabla es estática y se asume que ya contiene los 4 roles.
 */
@Entity(name = "AuthRoleEntity")
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Integer id;

    @Column(name = "role_type", nullable = false)
    private String roleType;
}
