package com.germogli.backend.authentication.web.controller;

import com.germogli.backend.authentication.application.dto.AuthResponseDTO;
import com.germogli.backend.authentication.application.dto.LoginRequestDTO;
import com.germogli.backend.authentication.application.dto.RegisterRequestDTO;
import com.germogli.backend.authentication.application.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para autenticación.
 * Exponde endpoints para login, registro y registro de administradores.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    /**
     * Endpoint para login.
     *
     * @param request DTO con las credenciales.
     * @return Token JWT de autenticación.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * Endpoint para registrar un usuario común.
     *
     * @param request DTO con los datos de registro.
     * @return Token JWT de autenticación.
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@RequestBody RegisterRequestDTO request) {
        return ResponseEntity.ok(authService.register(request));
    }

    /**
     * Endpoint protegido para registrar administradores.
     * Solo los usuarios con la autoridad ROLE_ADMINISTRADOR pueden acceder.
     *
     * @param request DTO con los datos de registro.
     * @return Token JWT de autenticación.
     */
    @PostMapping("/admin/register")
    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    public ResponseEntity<AuthResponseDTO> registerAdmin(@RequestBody RegisterRequestDTO request) {
        return ResponseEntity.ok(authService.registerAdmin(request));
    }
}
