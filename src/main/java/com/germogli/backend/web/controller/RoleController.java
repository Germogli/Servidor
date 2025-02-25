package com.germogli.backend.web.controller;

import com.germogli.backend.domain.user.dto.UpdateUserRoleDTO;
import com.germogli.backend.domain.user.service.RoleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/role")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PutMapping("/updateRole")
    public ResponseEntity<String> updateUserRole(@RequestBody UpdateUserRoleDTO updateUserRoleDTO) {
        roleService.updateUserRole(updateUserRoleDTO);
        return ResponseEntity.ok("Rol actualizado correctamente");
    }
}
