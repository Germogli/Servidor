package com.germogli.backend.user.role.domain.service;

import com.germogli.backend.user.role.application.dto.UpdateUserRoleDTO;
import com.germogli.backend.user.role.domain.repository.RoleDomainRepository;
import com.germogli.backend.user.role.infrastructure.crud.RoleCrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoleService {

    // Inyección del repositorio que gestiona las operaciones de rol.
    private final RoleDomainRepository roleDomainRepository;

    // Constructor para la inyección de dependencias, que asegura que el repositorio se pasa al servicio.
    public RoleService(RoleDomainRepository roleDomainRepository) {
        this.roleDomainRepository = roleDomainRepository;
    }

    @Transactional // Garantiza que la operación de actualización se realice de manera atómica.
    //Esto significa que todo el bloque de código dentro del método se tratará como una única unidad de trabajo
    public void updateUserRole(UpdateUserRoleDTO updateUserRoleDTO) {
        roleDomainRepository.updateUserRoleSP(updateUserRoleDTO);
    }
}
