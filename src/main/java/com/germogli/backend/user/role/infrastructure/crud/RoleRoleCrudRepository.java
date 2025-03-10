package com.germogli.backend.user.role.infrastructure.crud;

import com.germogli.backend.user.role.infrastructure.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRoleCrudRepository extends JpaRepository<RoleEntity, Integer> {
}
