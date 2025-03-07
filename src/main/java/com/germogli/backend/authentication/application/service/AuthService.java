package com.germogli.backend.authentication.application.service;

import com.germogli.backend.authentication.application.dto.AuthResponseDTO;
import com.germogli.backend.authentication.application.dto.LoginRequestDTO;
import com.germogli.backend.authentication.application.dto.RegisterRequestDTO;
import com.germogli.backend.authentication.domain.model.Role;
import com.germogli.backend.authentication.domain.model.UserDomain;
import com.germogli.backend.authentication.domain.repository.UserDomainRepository;
import com.germogli.backend.authentication.infrastructure.security.JwtService;
import com.germogli.backend.common.exception.UserAlreadyExistsException;
import com.germogli.backend.common.exception.UserNotFoundException;
import com.germogli.backend.common.exception.AdminAccessDeniedException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

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

        // Genera el token utilizando el UserDetails obtenido del dominio
        String token = jwtService.getToken(userDomain.toUserDetails());
        return AuthResponseDTO.builder().token(token).build();
    }

    public AuthResponseDTO register(RegisterRequestDTO request) {
        // Registro de usuarios comunes: asigna por defecto el rol "COMUN"
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException("El usuario " + request.getUsername() + " ya existe.");
        }

        // Se asigna el rol "COMUN"
        Role defaultRole = Role.builder()
                .id(4)
                .roleType("COMUN")
                .build();

        UserDomain userDomain = UserDomain.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .country(request.getCountry())
                .isActive(true)
                .role(defaultRole)
                .creationDate(LocalDateTime.now())
                .build();

        userRepository.save(userDomain);

        String token = jwtService.getToken(userDomain.toUserDetails());
        return AuthResponseDTO.builder().token(token).build();
    }

    /**
     * Registra un usuario administrador. Este método se invoca desde un endpoint protegido.
     * Se realiza una verificación adicional para asegurarse de que el usuario autenticado tenga la authority ADMINISTRADOR.
     */
    public AuthResponseDTO registerAdmin(RegisterRequestDTO request) {
        // Verifica que el usuario autenticado tenga la authority ADMINISTRADOR
        UserDetails currentUserDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!currentUserDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR"))) {
            throw new AdminAccessDeniedException("No se ha reconocido como administrador, no puede realizar esta acción.");
        }

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException("El usuario " + request.getUsername() + " ya existe.");
        }

        // Asigna el rol "ADMINISTRADOR"
        Role adminRole = Role.builder()
                .id(3)
                .roleType("ADMINISTRADOR")
                .build();

        UserDomain userDomain = UserDomain.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .country(request.getCountry())
                .isActive(true)
                .role(adminRole)
                .creationDate(LocalDateTime.now())
                .build();

        userRepository.save(userDomain);

        String token = jwtService.getToken(userDomain.toUserDetails());
        return AuthResponseDTO.builder().token(token).build();
    }
}
