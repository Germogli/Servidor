package com.germogli.backend.user.domain.repository;


import com.germogli.backend.user.application.dto.UpdateUserRoleDTO;

// Define el contrato para el consumo del SP
public interface RoleRepositoryCustom {
    void updateUserRoleSP(UpdateUserRoleDTO updateUserRoleDTO);
}
