package com.germogli.backend.authentication.web.controller;

import com.germogli.backend.authentication.application.dto.AuthResponseDTO;
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
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    /**
     * Endpoint para login que devuelve token en el cuerpo de la respuesta.
     * Mantiene compatibilidad con las implementaciones anteriores.
     *
     * @param request DTO con las credenciales.
     * @return Token JWT de autenticación.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * Endpoint para login optimizado que establece el token en una cookie HttpOnly.
     * Este enfoque es más seguro contra ataques XSS.
     *
     * @param request DTO con las credenciales.
     * @param response Respuesta HTTP donde se establecerá la cookie.
     * @return Información del usuario autenticado (sin el token).
     */
    @PostMapping("/login-secure")
    public ResponseEntity<UserInfoResponseDTO> loginSecure(
            @RequestBody LoginRequestDTO request,
            HttpServletResponse response) {
        return ResponseEntity.ok(authService.login(request, response));
    }

    /**
     * Endpoint para registro que devuelve token en el cuerpo de la respuesta.
     * Mantiene compatibilidad con las implementaciones anteriores.
     *
     * @param request DTO con los datos de registro.
     * @return Token JWT de autenticación.
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@RequestBody RegisterRequestDTO request) {
        return ResponseEntity.ok(authService.register(request));
    }

    /**
     * Endpoint para registro optimizado que establece el token en una cookie HttpOnly.
     * Este enfoque es más seguro contra ataques XSS.
     *
     * @param request DTO con los datos de registro.
     * @param response Respuesta HTTP donde se establecerá la cookie.
     * @return Información del usuario registrado (sin el token).
     */
    @PostMapping("/register-secure")
    public ResponseEntity<UserInfoResponseDTO> registerSecure(
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
     * @return Token JWT de autenticación.
     */
    @PostMapping("/admin/register")
    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    public ResponseEntity<AuthResponseDTO> registerAdmin(@RequestBody RegisterRequestDTO request) {
        return ResponseEntity.ok(authService.registerAdmin(request));
    }
}