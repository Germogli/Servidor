package com.germogli.backend.authentication.infrastructure.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * Mapea la tabla 'users' en la base de datos.
 * Se establece una relación ManyToOne con RoleEntity para reflejar la columna role_id.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "AuthUserEntity")
@Table(name = "users", uniqueConstraints = {@UniqueConstraint(columnNames = {"username"})})
public class UserEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column
    private String country;

    @Column
    private String avatar;

    @Column
    private String description;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    // Relación con el rol (se asume que la tabla roles ya está poblada)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private RoleEntity role;

    @Column(name = "creation_date", nullable = false)
    private LocalDateTime creationDate;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Asigna la autoridad basándose en el role, por ejemplo: ROLE_COMUN.
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.getRoleType().toUpperCase()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Puedes agregar lógica adicional si es necesario
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Puedes controlar el bloqueo de cuentas aquí
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Puedes agregar validaciones para credenciales expiradas
    }

    @Override
    public boolean isEnabled() {
        return isActive;
    }
}
