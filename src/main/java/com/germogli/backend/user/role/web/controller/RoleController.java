package com.germogli.backend.user.role.web.controller;

import com.germogli.backend.user.user.application.dto.ApiResponseDTO;
import com.germogli.backend.user.role.application.dto.UpdateUserRoleDTO;
import com.germogli.backend.user.role.domain.service.RoleDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class RoleController {

    private final RoleDomainService roleDomainService;

    @PutMapping("/update-user-role/{userId}")
    public ResponseEntity<ApiResponseDTO<UpdateUserRoleDTO>> updateUserRole(@PathVariable Integer userId, @RequestBody UpdateUserRoleDTO updateUserRoleDTO) {
        // Aseguramos que el ID del usuario en la ruta y en el DTO coincidan
        updateUserRoleDTO.setUserId(userId);
        roleDomainService.updateUserRole(updateUserRoleDTO);
        return ResponseEntity.ok(ApiResponseDTO.<UpdateUserRoleDTO>builder()
                .message("Rol de usuario actualizado correctamente")
                .data(updateUserRoleDTO)
                .build());
    }
}