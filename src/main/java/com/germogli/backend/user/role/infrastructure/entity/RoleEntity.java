package com.germogli.backend.user.role.infrastructure.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.germogli.backend.authentication.infrastructure.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// Anotaciones lombok para generar codigo repetitivo al correr la aplicacion
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "UseRoleEntity")
@Table(name = "roles")
public class RoleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Especifica que el valor se generará automáticamente por la base de datos (auto-incrementable).
    @Column(name = "role_id") // Mapea este campo a la columna "role_id" en la tabla "role"
    private Integer id;

    @Column(name = "role_type", nullable = false, unique = true)
    private String roleType;

    @OneToMany( // Define una relación Uno a Muchos (Un rol puede estar asociado a muchos usuarios)
            mappedBy = "role", // Indica que la relación está mapeada por el atributo "role" en la entidad User
            cascade = CascadeType.ALL, // Propaga todas las operaciones (persist, merge, remove, refresh, detach) a los usuarios asociados
            fetch = FetchType.LAZY // Carga los usuarios solo cuando se accede a ellos, optimizando el rendimiento
    )
    @JsonIgnore  // Evita problemas de serialización en JSON, como bucles infinitos al convertir objetos a JSON
    private List<UserEntity> users;
}
