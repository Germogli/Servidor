package com.germogli.backend.authentication.infrastructure.repository;

import com.germogli.backend.authentication.domain.model.UserDomain;
import com.germogli.backend.authentication.domain.repository.UserDomainRepository;
import com.germogli.backend.authentication.infrastructure.crud.AuthenticationUserCrudRepository;
import com.germogli.backend.authentication.infrastructure.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Implementación de UserDomainRepository.
 * Se encarga de transformar entre UserEntity y UserDomain.
 */
@Primary
@Repository("AuthenticationUserRepository")
@RequiredArgsConstructor
public class UserRepository implements UserDomainRepository {
    private final AuthenticationUserCrudRepository authenticationUserCrudRepository;

    @Override
    public Optional<UserDomain> findByUsername(String username) {
        return authenticationUserCrudRepository.findByUsername(username)
                .map(UserDomain::fromEntity);
    }

    @Override
    public UserDomain save(UserDomain user) {
        UserEntity entity = user.toEntity();
        UserEntity savedEntity = authenticationUserCrudRepository.save(entity);
        return UserDomain.fromEntity(savedEntity);
    }
}
