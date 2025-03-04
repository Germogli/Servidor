package com.germogli.backend.domain.user.repository;

import com.germogli.backend.infraestructure.persistence.persistenceUser.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IUserRepository extends JpaRepository<User, Integer> , UserRepositoryCustom {
}
