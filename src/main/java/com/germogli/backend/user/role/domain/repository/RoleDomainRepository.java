package com.germogli.backend.user.role.domain.repository;

import com.germogli.backend.user.role.domain.model.Role;

public interface RoleDomainRepository {
    void updateUserRole(Integer userId,Role role);
}
