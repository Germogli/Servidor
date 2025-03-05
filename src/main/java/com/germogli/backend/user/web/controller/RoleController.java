package com.germogli.backend.user.web.controller;


import com.germogli.backend.user.application.dto.UpdateUserRoleDTO;
import com.germogli.backend.user.application.service.RoleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/role")
public class RoleController {

    // Declaración de la variable para inyectar el servicio de correspondiente.
    // Este servicio se usará para procesar las operaciones solicitadas por los endpoints.
    private final RoleService roleService;

    // Constructor para inyección de dependencia del servicio correspondiente.
    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PutMapping("/updateRole")
    public ResponseEntity<String> updateUserRole(@RequestBody UpdateUserRoleDTO updateUserRoleDTO) {
        // Se invoca el método del servicio que ejecuta la lógica a través de un procedimiento almacenado.
        roleService.updateUserRole(updateUserRoleDTO);
        return ResponseEntity.ok("Rol actualizado correctamente");
    }
}
