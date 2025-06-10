package com.germogli.backend.user.role.infrastructure.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.germogli.backend.user.user.infrastructure.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity(name = "RoleRoleEntity")
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

    @OneToMany( // Define una relación Uno a Muchos (Un rol puede estar asociado a muchos usuarios)
            mappedBy = "role", // Indica que la relación está mapeada por el atributo "role" en la entidad User
            cascade = CascadeType.ALL, // Propaga todas las operaciones (persist, merge, remove, refresh, detach) a los usuarios asociados
            fetch = FetchType.LAZY // Carga los usuarios solo cuando se accede a ellos, optimizando el rendimiento
    )
    @JsonIgnore  // Evita problemas de serialización en JSON, como bucles infinitos al convertir objetos a JSON
    private List<UserEntity> users;
}
