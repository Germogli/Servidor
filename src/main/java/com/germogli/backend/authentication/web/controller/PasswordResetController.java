package com.germogli.backend.authentication.web.controller;
import com.germogli.backend.authentication.application.service.PasswordResetService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    /**
     * Endpoint para solicitar la recuperación de contraseña.
     * Se envía un email al usuario con un enlace para resetear la contraseña.
     *
     * Ejemplo de petición (POST):
     * URL: /auth/forgot-password?email=usuario@example.com
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam("email") String email) {
        passwordResetService.createPasswordResetTokenForUser(email);
        return ResponseEntity.ok("Se ha enviado un email para restablecer la contraseña.");
    }

    /**
     * Endpoint para reiniciar la contraseña.
     * Recibe el token y la nueva contraseña.
     *
     * Ejemplo de petición (POST):
     * URL: /auth/reset-password?token=TOKEN_GENERADO&newPassword=nuevaContraseña
     */
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam("token") String token,
                                                @RequestParam("newPassword") String newPassword) {
        passwordResetService.resetPassword(token, newPassword);
        return ResponseEntity.ok("La contraseña ha sido restablecida correctamente.");
    }
}