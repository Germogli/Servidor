package com.germogli.backend.authentication.web.controller;

import com.germogli.backend.authentication.application.dto.LoginRequestDTO;
import com.germogli.backend.authentication.application.dto.RegisterRequestDTO;
import com.germogli.backend.authentication.application.dto.UserInfoResponseDTO;
import com.germogli.backend.authentication.application.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para autenticación.
 * Expone endpoints para login, registro, logout y registro de administradores.
 * Implementa autenticación basada en cookies seguras.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    /**
     * Endpoint para login que establece el token en una cookie HttpOnly.
     *
     * @param request DTO con las credenciales.
     * @param response Respuesta HTTP donde se establecerá la cookie.
     * @return Información del usuario autenticado.
     */
    @PostMapping("/login")
    public ResponseEntity<UserInfoResponseDTO> login(
            @RequestBody LoginRequestDTO request,
            HttpServletResponse response) {
        return ResponseEntity.ok(authService.login(request, response));
    }

    /**
     * Endpoint para registro que establece el token en una cookie HttpOnly.
     *
     * @param request DTO con los datos de registro.
     * @param response Respuesta HTTP donde se establecerá la cookie.
     * @return Información del usuario registrado.
     */
    @PostMapping("/register")
    public ResponseEntity<UserInfoResponseDTO> register(
            @RequestBody RegisterRequestDTO request,
            HttpServletResponse response) {
        return ResponseEntity.ok(authService.register(request, response));
    }

    /**
     * Endpoint para cerrar sesión eliminando la cookie JWT.
     *
     * @param response Respuesta HTTP donde se eliminará la cookie.
     * @return Respuesta vacía con estado 200 OK.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        authService.logout(response);
        return ResponseEntity.ok().build();
    }

    /**
     * Endpoint protegido para registrar administradores.
     * Solo los usuarios con la autoridad ROLE_ADMINISTRADOR pueden acceder.
     *
     * @param request DTO con los datos de registro.
     * @param response Respuesta HTTP donde se establecerá la cookie.
     * @return Información del usuario administrador registrado.
     */
    @PostMapping("/admin/register")
    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    public ResponseEntity<UserInfoResponseDTO> registerAdmin(
            @RequestBody RegisterRequestDTO request,
            HttpServletResponse response) {
        return ResponseEntity.ok(authService.registerAdmin(request, response));
    }

    /**
     * Endpoint de comprobación de sesión.
     * Solo devuelve 200 OK si el usuario está autenticado;
     * Spring Security bloqueará con 401/403 si no lo está.
     */
    @GetMapping("/check")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> checkSession() {
        // No hace nada de lógica: si llegas aquí, tu sesión es válida
        return ResponseEntity.ok().build();
    }
}