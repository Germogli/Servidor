package com.germogli.backend.authentication.application.service;

import com.germogli.backend.authentication.application.dto.AuthResponseDTO;
import com.germogli.backend.authentication.application.dto.LoginRequestDTO;
import com.germogli.backend.authentication.application.dto.RegisterRequestDTO;
import com.germogli.backend.authentication.domain.model.Role;
import com.germogli.backend.authentication.domain.model.UserDomain;
import com.germogli.backend.authentication.domain.repository.UserDomainRepository;
import com.germogli.backend.authentication.infrastructure.security.JwtService;
import com.germogli.backend.common.email.EmailService;
import com.germogli.backend.common.exception.UserAlreadyExistsException;
import com.germogli.backend.common.exception.UserNotFoundException;
import com.germogli.backend.common.exception.AdminAccessDeniedException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

/**
 * Servicio de autenticación que maneja el login, registro y registro de administradores.
 * También genera los tokens JWT para los usuarios autenticados.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    // Repositorio para obtener y persistir usuarios. Se utiliza la cualificación para evitar conflictos de inyección.
    private final @Qualifier("AuthenticationUserRepository") UserDomainRepository userRepository;
    // Servicio para generar y validar JWT.
    private final JwtService jwtService;
    // Codificador de contraseñas.
    private final PasswordEncoder passwordEncoder;
    // Gestor de autenticación para verificar credenciales.
    private final AuthenticationManager authenticationManager;
    // Servicio para enviar emails.
    private final EmailService emailService;

    /**
     * Autentica al usuario y genera un token JWT.
     *
     * @param request DTO con las credenciales del usuario.
     * @return DTO de respuesta con el token generado.
     * @throws UserNotFoundException Si el usuario no existe.
     * @throws BadCredentialsException Si la contraseña es incorrecta.
     */
    public AuthResponseDTO login(LoginRequestDTO request) {
        // Se busca el usuario por su nombre de usuario.
        UserDomain userDomain = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UserNotFoundException("El usuario " + request.getUsername() + " no está registrado"));

        // Se intenta autenticar usando el AuthenticationManager.
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (BadCredentialsException ex) {
            // Si la autenticación falla, se lanza una excepción con mensaje personalizado.
            throw new BadCredentialsException("La contraseña ingresada es incorrecta.");
        }

        // Se genera el token JWT usando el UserDetails del usuario.
        String token = jwtService.getToken(userDomain.toUserDetails());
        return AuthResponseDTO.builder().token(token).build();
    }

    /**
     * Registra un usuario común.
     *
     * @param request DTO con los datos de registro.
     * @return DTO de respuesta con el token JWT generado.
     * @throws UserAlreadyExistsException Si el usuario ya existe.
     */
    public AuthResponseDTO register(RegisterRequestDTO request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException("El usuario " + request.getUsername() + " ya existe.");
        }

        if(userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("El correo " + request.getEmail() + " ya se encuentra registrado en la plataforma.");
        }

        // Asigna el rol por defecto "COMUN"
        Role defaultRole = Role.builder()
                .id(4)
                .roleType("COMUN")
                .build();

        // Crea el objeto de dominio del usuario, codificando la contraseña.
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
        // Enviar email de confirmación
        String subject = "Bienvenido a Germogli";
        String text = "Hola " + userDomain.getUsername() + ",\n\nGracias por registrarte en Germogli. ¡Cultiva conocimiento, cosecha comunidad!";

        emailService.sendSimpleMessage(userDomain.getEmail(), subject, text);

        // Genera el token JWT para el usuario registrado.
        String token = jwtService.getToken(userDomain.toUserDetails());
        return AuthResponseDTO.builder().token(token).build();
    }

    /**
     * Registra un usuario administrador.
     * Este endpoint está protegido y solo puede ser accedido por usuarios con rol ADMINISTRADOR.
     *
     * @param request DTO con los datos de registro.
     * @return DTO de respuesta con el token JWT generado.
     * @throws AdminAccessDeniedException Si el usuario autenticado no es administrador.
     * @throws UserAlreadyExistsException Si el usuario ya existe.
     */
    public AuthResponseDTO registerAdmin(RegisterRequestDTO request) {
        // Obtiene el usuario autenticado del contexto de seguridad.
        UserDetails currentUserDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // Verifica que el usuario tenga la autoridad ADMINISTRADOR.
        if (!currentUserDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR"))) {
            throw new AdminAccessDeniedException("No se ha reconocido como administrador, no puede realizar esta acción.");
        }

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException("El usuario " + request.getUsername() + " ya existe.");
        }

        // Asigna el rol de administrador.
        Role adminRole = Role.builder()
                .id(3)
                .roleType("ADMINISTRADOR")
                .build();

        // Crea el objeto de dominio para el administrador.
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

        // Genera el token JWT para el administrador.
        String token = jwtService.getToken(userDomain.toUserDetails());
        return AuthResponseDTO.builder().token(token).build();
    }
}
