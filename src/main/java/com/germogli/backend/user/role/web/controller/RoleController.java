package com.germogli.backend.user.role.web.controller;

import com.germogli.backend.user.role.application.dto.ApiResponseDTO;
import com.germogli.backend.user.role.application.dto.UpdateUserRoleDTO;
import com.germogli.backend.user.role.domain.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/role")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @PutMapping("/updateRole")
    public ResponseEntity<ApiResponseDTO<UpdateUserRoleDTO>> updateUserRole(@RequestBody UpdateUserRoleDTO updateUserRoleDTO) {
        roleService.updateUserRole(updateUserRoleDTO);
        return ResponseEntity.ok(ApiResponseDTO.<UpdateUserRoleDTO>builder()
                .message("Rol actualizado correctamente")
                .data(updateUserRoleDTO)
                .build());
    }
}
