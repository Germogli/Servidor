package com.germogli.backend.user.domain.repository;

import com.germogli.backend.infraestructure.persistence.persistenceUser.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

// Extiende JpaRepository para operaciones CRUD básicas y la interfaz custom para SP.
public interface RoleRepository extends JpaRepository<Role, Integer> , RoleRepositoryCustom {
}
