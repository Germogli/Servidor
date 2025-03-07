package com.germogli.backend.user.user.infrastructure.crud;

import com.germogli.backend.user.user.infrastructure.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRoleCrudRepository extends JpaRepository<RoleEntity, Integer> {
    Optional<RoleEntity> findByRoleType(String roleType);
}
