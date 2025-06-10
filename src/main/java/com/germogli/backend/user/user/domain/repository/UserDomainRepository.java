package com.germogli.backend.user.user.domain.repository;

import com.germogli.backend.user.user.domain.model.User;

import java.util.List;

/**
 * Contrato para las operaciones de usuario mediante procedimientos almacenados.
 */
public interface UserDomainRepository {
    void updateUserInfo(User user);
    void deleteUser(User user);
    User getUserByUsername(String username);

    // Nuevo método para obtener un usuario por su ID
    User getUserById(Integer id);

    /**
     * Obtiene todos los usuarios del sistema.
     * Esta operación debe ser restringida solo para administradores.
     *
     * @return Lista de todos los usuarios registrados.
     */
    List<User> getAllUsers();
}
