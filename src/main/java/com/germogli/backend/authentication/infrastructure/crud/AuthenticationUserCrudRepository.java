package com.germogli.backend.authentication.infrastructure.crud;

import com.germogli.backend.authentication.infrastructure.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositorio CRUD para la entidad UserEntity.
 */
public interface AuthenticationUserCrudRepository extends JpaRepository<UserEntity, Integer> {
    Optional<UserEntity> findByUsername(String username);
}
