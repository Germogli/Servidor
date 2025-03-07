package com.germogli.backend.user.role.domain.repository;


import com.germogli.backend.user.role.application.dto.UpdateUserRoleDTO;
import org.springframework.data.jpa.repository.query.Procedure;

// Define el contrato para el consumo del SP
public interface RoleDomainRepository {
    void updateUserRoleSP(UpdateUserRoleDTO updateUserRoleDTO);
}
