package com.germogli.backend.user.application.service;


import com.germogli.backend.user.application.dto.UpdateUserRoleDTO;
import com.germogli.backend.user.domain.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoleService {

    // Inyección del repositorio que gestiona las operaciones de rol.
    private final RoleRepository roleRepository;

    // Constructor para la inyección de dependencias, que asegura que el repositorio se pasa al servicio.
    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Transactional // Garantiza que la operación de actualización se realice de manera atómica.
    //Esto significa que todo el bloque de código dentro del método se tratará como una única unidad de trabajo
    public void updateUserRole(UpdateUserRoleDTO updateUserRoleDTO) {
        roleRepository.updateUserRoleSP(updateUserRoleDTO);
    }
}
