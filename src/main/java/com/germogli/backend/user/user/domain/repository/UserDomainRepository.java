package com.germogli.backend.user.user.domain.repository;

import com.germogli.backend.user.user.domain.model.User;

/**
 * Contrato para las operaciones de usuario mediante procedimientos almacenados.
 */
public interface UserDomainRepository {
    void updateUserInfo(User user);
    void deleteUser(User user);
    User getUserByUsername(String username);

    // Nuevo método para obtener un usuario por su ID
    User getUserById(Integer id);
}
