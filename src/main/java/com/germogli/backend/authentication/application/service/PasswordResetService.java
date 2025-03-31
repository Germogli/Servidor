package com.germogli.backend.authentication.application.service;
import com.germogli.backend.authentication.domain.model.UserDomain;
import com.germogli.backend.authentication.domain.repository.UserDomainRepository;
import com.germogli.backend.authentication.infrastructure.crud.PasswordResetTokenRepository;
import com.germogli.backend.authentication.infrastructure.entity.PasswordResetToken;
import com.germogli.backend.common.email.EmailService;
import com.germogli.backend.common.exception.ResourceNotFoundException;
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

    @Transactional
    public void createPasswordResetTokenForUser(String email) {
        // Buscar usuario por email (asegúrate de implementar findByEmail en tu repositorio)
        UserDomain user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + email));

        // Generar token y establecer fecha de expiración (ejemplo: 1 hora)
        String token = UUID.randomUUID().toString();
        LocalDateTime expirationDate = LocalDateTime.now().plusHours(1);

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .userId(user.getId())
                .expirationDate(expirationDate)
                .build();

        tokenRepository.save(resetToken);

        // Construir el enlace para restablecer la contraseña (ajusta la URL a tu front-end)
        String resetUrl = "http://tusitio.com/reset-password?token=" + token;
        String subject = "Recuperación de Contraseña";
        String text = "Hola " + user.getUsername() + ",\n\n" +
                "Para restablecer tu contraseña, haz clic en el siguiente enlace:\n" +
                resetUrl + "\n\n" +
                "Este enlace expirará en 1 hora.\n\nSaludos,\nEquipo Germogli";

        emailService.sendSimpleMessage(user.getEmail(), subject, text);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Token no válido o expirado."));

        if (resetToken.getExpirationDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("El token ha expirado.");
        }

        UserDomain user = userRepository.findById(resetToken.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado."));

        // Actualizar la contraseña encriptándola
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Eliminar el token después de su uso
        tokenRepository.delete(resetToken);
    }
}