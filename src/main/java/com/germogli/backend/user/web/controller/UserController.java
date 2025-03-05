package com.germogli.backend.user.web.controller;


import com.germogli.backend.infraestructure.persistence.persistenceUser.entity.User;
import com.germogli.backend.user.application.dto.DeleteUserDTO;
import com.germogli.backend.user.application.dto.GetUserByEmailDTO;
import com.germogli.backend.user.application.dto.UpdateUserInfoDTO;
import com.germogli.backend.user.application.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    // Declaración de la variable para inyectar el servicio de correspondiente.
    // Este servicio se usará para procesar las operaciones solicitadas por los endpoints.
    private final UserService userService;

    // Constructor para inyección de dependencia del servicio correspondiente.
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PutMapping("/updateInfo")
    public ResponseEntity<String> updateUserInfo(@RequestBody UpdateUserInfoDTO updateUserInfoDTO) {
        // Se invoca el método del servicio que ejecuta la lógica a través de un procedimiento almacenado.
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
        // Se crea el DTO directamente usando el constructor que recibe el email
        User user = userService.getUserByEmail(new GetUserByEmailDTO(email));
        return ResponseEntity.ok(user);
    }

}
