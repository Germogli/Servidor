package com.germogli.backend.user.user.infrastructure.crud;

import com.germogli.backend.user.user.infrastructure.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserUserCrudRepository extends JpaRepository<UserEntity, Integer> {
    Optional<UserEntity> findByUsername(String username);
}
