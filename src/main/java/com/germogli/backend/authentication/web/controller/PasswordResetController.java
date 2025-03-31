package com.germogli.backend.authentication.web.controller;
import com.germogli.backend.authentication.application.dto.PasswordResetDTO;
import com.germogli.backend.authentication.application.dto.PasswordResetResponseDTO;
import com.germogli.backend.authentication.application.service.PasswordResetService;

import jakarta.validation.Valid;
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
     * URL: /auth/forgot-password?email=usuario@example.com
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<PasswordResetResponseDTO> forgotPassword(@RequestParam("email") String email) {
        String token = passwordResetService.createPasswordResetTokenForUser(email);
        return ResponseEntity.ok(new PasswordResetResponseDTO("Se ha enviado un email para restablecer la contraseña.", token));
    }

    /**
     * Endpoint para reiniciar la contraseña.
     *
     */
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody PasswordResetDTO request) {
        passwordResetService.resetPassword(request);
        return ResponseEntity.ok("La contraseña ha sido restablecida correctamente.");
    }
}