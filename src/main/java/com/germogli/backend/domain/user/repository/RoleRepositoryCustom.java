package com.germogli.backend.domain.user.repository;

import com.germogli.backend.domain.user.dto.UpdateUserRoleDTO;

public interface RoleRepositoryCustom {
    void updateUserRoleSP(UpdateUserRoleDTO updateUserRoleDTO);
}
