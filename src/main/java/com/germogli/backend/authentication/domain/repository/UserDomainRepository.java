package com.germogli.backend.authentication.domain.repository;

import com.germogli.backend.authentication.domain.model.UserDomain;

import java.util.Optional;

/**
 * Interfaz que define las operaciones de persistencia para el usuario.
 */
public interface UserDomainRepository {
    Optional<UserDomain> findByUsername(String username);
    UserDomain save(UserDomain user);
}
