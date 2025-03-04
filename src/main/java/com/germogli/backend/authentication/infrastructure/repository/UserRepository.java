package com.germogli.backend.authentication.infrastructure.repository;

import com.germogli.backend.authentication.domain.model.UserDomain;
import com.germogli.backend.authentication.domain.repository.UserDomainRepository;
import com.germogli.backend.authentication.infrastructure.crud.UserCrudRepository;
import com.germogli.backend.authentication.infrastructure.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepository implements UserDomainRepository {
    private final UserCrudRepository userCrudRepository;

    @Override
    public Optional<UserDomain> findByUsername(String username) {
        return userCrudRepository.findByUsername(username)
                .map(UserDomain::fromEntity);
    }

    @Override
    public UserDomain save(UserDomain user) {
        UserEntity entity = user.toEntity();
        UserEntity savedEntity = userCrudRepository.save(entity);
        return UserDomain.fromEntity(savedEntity);
    }
}
