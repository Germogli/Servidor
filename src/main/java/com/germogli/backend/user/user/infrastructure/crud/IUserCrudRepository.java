package com.germogli.backend.user.user.infrastructure.crud;

import com.germogli.backend.authentication.infrastructure.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

// Extiende JpaRepository para operaciones CRUD básicas y la interfaz custom para SP.
public interface IUserCrudRepository extends JpaRepository<UserEntity, Integer> {
}
