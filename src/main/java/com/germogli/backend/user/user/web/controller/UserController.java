package com.germogli.backend.user.user.web.controller;

import com.germogli.backend.user.user.application.dto.ApiResponseDTO;
import com.germogli.backend.user.user.application.dto.DeleteUserDTO;
import com.germogli.backend.user.user.application.dto.GetUserByUsernameDTO;
import com.germogli.backend.user.user.application.dto.UpdateUserInfoDTO;
import com.germogli.backend.user.user.domain.model.User;
import com.germogli.backend.user.user.domain.service.UserDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
