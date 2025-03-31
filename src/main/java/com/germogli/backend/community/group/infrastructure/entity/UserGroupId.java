package com.germogli.backend.community.group.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Clave compuesta para la entidad UserGroupEntity que representa la relación
 * entre un usuario y un grupo.
 */
@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserGroupId implements Serializable {

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "group_id")
    private Integer groupId;
}
