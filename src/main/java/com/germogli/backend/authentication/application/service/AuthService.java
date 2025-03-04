package com.germogli.backend.authentication.application.service;

import com.germogli.backend.authentication.application.dto.AuthResponseDTO;
import com.germogli.backend.authentication.application.dto.LoginRequestDTO;
import com.germogli.backend.authentication.application.dto.RegisterRequestDTO;
import com.germogli.backend.authentication.domain.model.UserDomain;
import com.germogli.backend.authentication.domain.repository.UserDomainRepository;
import com.germogli.backend.authentication.infrastructure.security.JwtService;
import com.germogli.backend.common.exception.UserAlreadyExistsException;
import com.germogli.backend.common.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserDomainRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthResponseDTO login(LoginRequestDTO request) {
        // Verifica que el usuario exista en el dominio
        UserDomain userDomain = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UserNotFoundException("El usuario " + request.getUsername() + " no está registrado"));

        // Autentica las credenciales; si fallan se lanzará una excepción
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        // Genera el token utilizando el objeto de dominio convertido a UserDetails
        String token = jwtService.getToken(userDomain.toUserDetails());
        return AuthResponseDTO.builder().token(token).build();
    }

    public AuthResponseDTO register(RegisterRequestDTO request) {
        // Verifica que no exista ya el usuario
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException("El usuario con el email " + request.getUsername() + " ya existe.");
        }

        // Crea el objeto de dominio a partir del DTO (codifica la contraseña)
        UserDomain userDomain = UserDomain.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .country(request.getCountry())
                .isActive(true)
                .role(com.germogli.backend.authentication.domain.model.Role.COMUN)  // Rol por defecto
                .creationDate(java.time.LocalDateTime.now())
                .build();

        // Persiste el objeto de dominio a través del repositorio del dominio
        userRepository.save(userDomain);

        // Genera el token
        String token = jwtService.getToken(userDomain.toUserDetails());
        return AuthResponseDTO.builder().token(token).build();
    }
}
