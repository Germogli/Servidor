package com.germogli.backend.web.controller;

import com.germogli.backend.domain.user.dto.DeleteUserDTO;
import com.germogli.backend.domain.user.dto.GetUserByEmailDTO;
import com.germogli.backend.domain.user.dto.UpdateUserInfoDTO;
import com.germogli.backend.domain.user.service.UserService;
import com.germogli.backend.infraestructure.persistence.persistenceUser.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PutMapping("/updateInfo")
    public ResponseEntity<String> updateUserInfo(@RequestBody UpdateUserInfoDTO updateUserInfoDTO) {
        userService.updateUserInfo(updateUserInfoDTO);
        return ResponseEntity.ok("Información de usuario actualizada correctamente");
    }

    @DeleteMapping("/deleteUser")
    public ResponseEntity<String> deleteUser(@RequestBody DeleteUserDTO deleteUserDTO) {
        userService.deleteUser(deleteUserDTO);
        return ResponseEntity.ok("Usuario eliminado correctamente");
    }

    @GetMapping("/getUser")
    public ResponseEntity<User> getUserByEmail(@RequestParam String email) {
        GetUserByEmailDTO dto = new GetUserByEmailDTO();
        dto.setEmail(email);
        User user = userService.getUserByEmail(dto);
        return ResponseEntity.ok(user);
    }

}
