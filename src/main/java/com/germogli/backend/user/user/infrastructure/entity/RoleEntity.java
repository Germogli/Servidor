package com.germogli.backend.user.user.infrastructure.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity(name = "UserRoleEntity")
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
