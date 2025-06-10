package com.germogli.backend.authentication.application.service;

import com.germogli.backend.authentication.application.dto.PasswordResetDTO;
import com.germogli.backend.authentication.domain.model.UserDomain;
import com.germogli.backend.authentication.domain.repository.UserDomainRepository;
import com.germogli.backend.authentication.infrastructure.crud.PasswordResetTokenRepository;
import com.germogli.backend.authentication.infrastructure.entity.PasswordResetToken;
import com.germogli.backend.authentication.infrastructure.security.JwtCookieManager;
import com.germogli.backend.authentication.infrastructure.security.JwtService;
import com.germogli.backend.common.email.EmailService;
import com.germogli.backend.common.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserDomainRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtCookieManager jwtCookieManager;
    private final JwtService jwtService;

    /**
     * Genera un token para restablecer la contraseña y envía un email al usuario.
     *
     * @param email Email del usuario que solicita restablecer su contraseña
     * @return Token generado para el restablecimiento
     * @throws ResourceNotFoundException Si no se encuentra el usuario con el email proporcionado
     */
    @Transactional
    public String createPasswordResetTokenForUser(String email) {
        UserDomain user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + email));

        String token = UUID.randomUUID().toString();
        LocalDateTime expirationDate = LocalDateTime.now().plusHours(1);

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .userId(user.getId())
                .expirationDate(expirationDate)
                .build();

        tokenRepository.save(resetToken);

        // Construir el enlace para restablecer la contraseña
        // Cambiar URL cuando se encuentre desplegado
        String resetUrl = "http://localhost:5173/reset-password?token=" + token;
        String subject = "Recuperación de Contraseña";
        String text = "Hola " + user.getUsername() + ",\n\n" +
                "Para restablecer tu contraseña, haz clic en el siguiente enlace:\n" +
                resetUrl + "\n\n" +
                "Este enlace expirará en 1 hora.\n\nSaludos,\nEquipo Germogli";

        emailService.sendSimpleMessage(user.getEmail(), subject, text);

        return token;
    }

    /**
     * Restablece la contraseña del usuario y establece una cookie de autenticación.
     *
     * @param request DTO con el token y la nueva contraseña
     * @param response Respuesta HTTP para establecer la cookie de autenticación
     * @throws ResourceNotFoundException Si el token no es válido o ha expirado
     */
    @Transactional
    public void resetPassword(PasswordResetDTO request, HttpServletResponse response) {
        PasswordResetToken resetToken = tokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new ResourceNotFoundException("Token no válido o expirado."));

        if (resetToken.getExpirationDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("El token ha expirado.");
        }

        UserDomain user = userRepository.findById(resetToken.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado."));

        // Actualizar la contraseña encriptándola
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Eliminar el token después de su uso
        tokenRepository.delete(resetToken);

        // Generar un token JWT y establecerlo en una cookie para autenticar al usuario
        String jwtToken = jwtService.getTokenWithRole(
                user.toUserDetails(),
                user.getRole().getRoleType()
        );

        jwtCookieManager.addJwtCookie(response, jwtToken);
    }
}