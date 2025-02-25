package com.germogli.backend.domain.user.service;

import com.germogli.backend.domain.user.dto.UpdateUserRoleDTO;
import com.germogli.backend.domain.user.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoleService {

    // Inyeccion del repositorio de rol
    private final RoleRepository roleRepository;

    // Constructor para inyeccion de dependencias
    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    // metodos que orquestan las funciones de los roles usando SP
    @Transactional
    public void updateUserRole(UpdateUserRoleDTO updateUserRoleDTO) {
        roleRepository.updateUserRoleSP(updateUserRoleDTO);
    }
}
