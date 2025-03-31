package com.germogli.backend.community.group.infrastructure.crud;

import com.germogli.backend.community.group.infrastructure.entity.UserGroupEntity;
import com.germogli.backend.community.group.infrastructure.entity.UserGroupId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserGroupCrudRepository extends JpaRepository<UserGroupEntity, UserGroupId> {
    // Métodos adicionales de consulta, si se requieren
}
