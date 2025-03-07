package com.germogli.backend.user.role.infrastructure.crud;

import com.germogli.backend.user.role.infrastructure.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

// Extiende JpaRepository para operaciones CRUD básicas y la interfaz custom para SP.
public interface RoleCrudRepository extends JpaRepository<RoleEntity, Integer> {
}
