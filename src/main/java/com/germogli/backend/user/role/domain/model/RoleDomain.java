package com.germogli.backend.user.role.domain.model;

import com.germogli.backend.authentication.domain.model.UserDomain;
import com.germogli.backend.user.role.infrastructure.entity.RoleEntity;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class RoleDomain {
    private Integer id;
    private String roleType;
    private List<UserDomain> users;

    /**
     * Convierte una RoleEntity en un objeto RoleDomain.
     */
    public static RoleDomain fromEntity(RoleEntity entity) {
        return RoleDomain.builder()
                .id(entity.getId())
                .roleType(entity.getRoleType())
                .users(entity.getUsers().stream()
                        .map(UserDomain::fromEntity)
                        .collect(Collectors.toList()))
                .build();
    }

    /**
     * Convierte este objeto RoleDomain en una RoleEntity para persistencia.
     */
    public RoleEntity toEntity() {
        return RoleEntity.builder()
                .id(id)
                .roleType(roleType)
                .users(users.stream()
                        .map(UserDomain::toEntity)
                        .collect(Collectors.toList()))
                .build();
    }
}
