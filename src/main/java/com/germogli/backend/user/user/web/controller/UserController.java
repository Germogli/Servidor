package com.germogli.backend.user.user.web.controller;

import com.germogli.backend.user.user.application.dto.*;
import com.germogli.backend.user.user.domain.model.User;
import com.germogli.backend.user.user.domain.service.UserDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserDomainService userDomainService;

    @PutMapping("/update/{userId}")
    public ResponseEntity<ApiResponseDTO<UpdateUserInfoDTO>> updateUserInfo(
            @PathVariable Integer userId,
            @RequestBody UpdateUserInfoDTO updateUserInfoDTO) {
        // Aseguramos que el ID en la ruta y en el DTO coincidan
        updateUserInfoDTO.setUserId(userId);
        userDomainService.updateUserInfo(updateUserInfoDTO);
        return ResponseEntity.ok(ApiResponseDTO.<UpdateUserInfoDTO>builder()
                .message("Información de usuario actualizada correctamente")
                .data(updateUserInfoDTO)
                .build());
    }

    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteUser(@PathVariable Integer userId) {
        DeleteUserDTO deleteUserDTO = new DeleteUserDTO(userId);
        userDomainService.deleteUser(deleteUserDTO);
        return ResponseEntity.ok(ApiResponseDTO.<Void>builder()
                .message("Usuario eliminado correctamente")
                .build());
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<ApiResponseDTO<UpdateUserInfoDTO>> getUserByUsername(@PathVariable String username) {
        User user = userDomainService.getUserByUsername(new GetUserByUsernameDTO(username));
        return ResponseEntity.ok(ApiResponseDTO.<UpdateUserInfoDTO>builder()
                .message("Usuario recuperado correctamente")
                .data(userDomainService.toResponseDTO(user))
                .build());
    }

    /**
     * Obtiene un usuario por su ID.
     *
     * @param userId ID del usuario a buscar.
     * @return ResponseEntity con la información del usuario en un ApiResponseDTO.
     */
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponseDTO<UpdateUserInfoDTO>> getUserById(@PathVariable Integer userId) {
        User user = userDomainService.getUserById(userId);
        return ResponseEntity.ok(ApiResponseDTO.<UpdateUserInfoDTO>builder()
                .message("Usuario recuperado correctamente")
                .data(userDomainService.toResponseDTO(user))
                .build());
    }

    /**
     * Obtiene todos los usuarios del sistema.
     * ENDPOINT RESTRINGIDO: Solo usuarios con rol ADMINISTRADOR pueden acceder.
     *
     * @return ResponseEntity con la lista de todos los usuarios en un ApiResponseDTO.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    public ResponseEntity<ApiResponseDTO<List<UserResponseDTO>>> getAllUsers() {
        List<User> users = userDomainService.getAllUsers();
        List<UserResponseDTO> userResponseDTOs = userDomainService.toUserResponseDTOList(users);

        return ResponseEntity.ok(ApiResponseDTO.<List<UserResponseDTO>>builder()
                .message("Usuarios recuperados correctamente")
                .data(userResponseDTOs)
                .build());
    }
}
