package com.germogli.backend.domain.user.repository;

import com.germogli.backend.infraestructure.persistence.persistenceUser.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Integer> , RoleRepositoryCustom {
}
