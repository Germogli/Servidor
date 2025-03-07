package com.germogli.backend.authentication.domain.model;

import com.germogli.backend.authentication.infrastructure.entity.UserEntity;
import lombok.*;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;

/**
 * Modelo de dominio para el usuario.
 * Contiene un objeto Role para reflejar la relación con la tabla roles.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDomain {
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
    private Role role;  // Relación con el rol (modelo de dominio)
    private LocalDateTime creationDate;

    /**
     * Convierte este objeto de dominio en un UserDetails para Spring Security.
     * Se asigna la autoridad basada en el rol, por ejemplo: ROLE_COMUN.
     */
    public UserDetails toUserDetails() {
        return User.builder()
                .username(username)
                .password(password)
                .authorities("ROLE_" + role.getRoleType().toUpperCase())
                .build();
    }

    /**
     * Factory method para crear un UserDomain a partir de una entidad de persistencia.
     */
    public static UserDomain fromEntity(UserEntity entity) {
        return UserDomain.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .email(entity.getEmail())
                .password(entity.getPassword())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .country(entity.getCountry())
                .avatar(entity.getAvatar())
                .description(entity.getDescription())
                .isActive(entity.getIsActive())
                .role(Role.builder()
                        .id(entity.getRole().getId())
                        .roleType(entity.getRole().getRoleType())
                        .build())
                .creationDate(entity.getCreationDate())
                .build();
    }

    /**
     * Convierte este objeto de dominio en una entidad de persistencia.
     */
    public UserEntity toEntity() {
        return UserEntity.builder()
                .id(id)
                .username(username)
                .email(email)
                .password(password)
                .firstName(firstName)
                .lastName(lastName)
                .country(country)
                .avatar(avatar)
                .description(description)
                .isActive(isActive)
                // Mapea el Role del dominio a RoleEntity
                .role(com.germogli.backend.authentication.infrastructure.entity.RoleEntity.builder()
                        .id(role.getId())
                        .roleType(role.getRoleType())
                        .build())
                .creationDate(creationDate)
                .build();
    }
}
