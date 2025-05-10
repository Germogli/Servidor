package com.germogli.backend.authentication.web.controller;

import com.germogli.backend.authentication.application.dto.PasswordResetDTO;
import com.germogli.backend.authentication.application.dto.PasswordResetResponseDTO;
import com.germogli.backend.authentication.application.service.PasswordResetService;
import jakarta.servlet.http.HttpServletResponse;
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
     *
     * @param email Email del usuario que solicita recuperar su contraseña
     * @return Respuesta con mensaje de confirmación y token generado
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<PasswordResetResponseDTO> forgotPassword(@RequestParam("email") String email) {
        String token = passwordResetService.createPasswordResetTokenForUser(email);
        return ResponseEntity.ok(new PasswordResetResponseDTO("Se ha enviado un email para restablecer la contraseña.", token));
    }

    /**
     * Endpoint para reiniciar la contraseña.
     * Este endpoint también establece una cookie de autenticación para iniciar
     * sesión automáticamente después de reestablecer la contraseña.
     *
     * @param request DTO con token y nueva contraseña
     * @param response Respuesta HTTP donde se establecerá la cookie
     * @return Mensaje de confirmación
     */
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
            @RequestBody PasswordResetDTO request,
            HttpServletResponse response) {
        passwordResetService.resetPassword(request, response);
        return ResponseEntity.ok("La contraseña ha sido restablecida correctamente.");
    }
}