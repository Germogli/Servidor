package com.germogli.backend.authentication.application.service;

import com.germogli.backend.authentication.application.dto.RegisterRequestDTO;
import com.germogli.backend.authentication.application.dto.LoginRequestDTO;
import com.germogli.backend.authentication.application.dto.UserInfoResponseDTO;
import com.germogli.backend.authentication.domain.model.Role;
import com.germogli.backend.authentication.domain.model.UserDomain;
import com.germogli.backend.authentication.domain.repository.UserDomainRepository;
import com.germogli.backend.authentication.infrastructure.security.JwtCookieManager;
import com.germogli.backend.authentication.infrastructure.security.JwtService;
import com.germogli.backend.common.email.EmailService;
import com.germogli.backend.common.exception.UserAlreadyExistsException;
import com.germogli.backend.common.exception.UserNotFoundException;
import com.germogli.backend.common.exception.AdminAccessDeniedException;
import jakarta.servlet.http.HttpServletResponse;
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
 * Implementa autenticación basada en cookies seguras.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final @Qualifier("AuthenticationUserRepository") UserDomainRepository userRepository;
    private final JwtService jwtService;
    private final JwtCookieManager jwtCookieManager;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    /**
     * Crea un DTO con información básica del usuario (sin datos sensibles)
     * para devolver tras autenticación exitosa.
     *
     * @param userDomain Datos del usuario autenticado
     * @return DTO con información segura del usuario
     */
    private UserInfoResponseDTO createUserInfoResponse(UserDomain userDomain) {
        return UserInfoResponseDTO.builder()
                .id(userDomain.getId())
                .username(userDomain.getUsername())
                .email(userDomain.getEmail())
                .firstName(userDomain.getFirstName())
                .lastName(userDomain.getLastName())
                .role(userDomain.getRole().getRoleType())
                .build();
    }

    /**
     * Autentica al usuario, genera un token JWT y lo establece en una cookie HttpOnly.
     *
     * @param request DTO con las credenciales del usuario.
     * @param response Respuesta HTTP para establecer la cookie.
     * @return DTO con información del usuario autenticado.
     * @throws UserNotFoundException Si el usuario no existe.
     * @throws BadCredentialsException Si la contraseña es incorrecta.
     */
    public UserInfoResponseDTO login(LoginRequestDTO request, HttpServletResponse response) {
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

        // Se genera el token JWT incluyendo el rol del usuario.
        String token = jwtService.getTokenWithRole(
                userDomain.toUserDetails(),
                userDomain.getRole().getRoleType()
        );

        // Establecer el token en una cookie HttpOnly
        jwtCookieManager.addJwtCookie(response, token);

        // Devolver información del usuario (sin el token)
        return createUserInfoResponse(userDomain);
    }

    /**
     * Registra un usuario común, genera un token JWT y lo establece en una cookie HttpOnly.
     *
     * @param request DTO con los datos de registro.
     * @param response Respuesta HTTP para establecer la cookie.
     * @return DTO con información del usuario registrado.
     * @throws UserAlreadyExistsException Si el usuario ya existe.
     */
    public UserInfoResponseDTO register(RegisterRequestDTO request, HttpServletResponse response) {
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

        // Genera el token JWT para el usuario registrado y lo establece en cookie
        String token = jwtService.getTokenWithRole(
                userDomain.toUserDetails(),
                userDomain.getRole().getRoleType()
        );

        jwtCookieManager.addJwtCookie(response, token);

        return createUserInfoResponse(userDomain);
    }

    /**
     * Registra un usuario administrador con token JWT en cookie.
     * Este endpoint está protegido y solo puede ser accedido por usuarios con rol ADMINISTRADOR.
     *
     * @param request DTO con los datos de registro.
     * @param response Respuesta HTTP donde se establecerá la cookie
     * @return DTO con información del usuario registrado.
     * @throws AdminAccessDeniedException Si el usuario autenticado no es administrador.
     * @throws UserAlreadyExistsException Si el usuario ya existe.
     */
    public UserInfoResponseDTO registerAdmin(RegisterRequestDTO request, HttpServletResponse response) {
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

        // Genera el token JWT para el administrador y lo establece en cookie
        String token = jwtService.getTokenWithRole(
                userDomain.toUserDetails(),
                userDomain.getRole().getRoleType()
        );

        jwtCookieManager.addJwtCookie(response, token);

        return createUserInfoResponse(userDomain);
    }

    /**
     * Cierra la sesión del usuario invalidando la cookie JWT.
     *
     * @param response Respuesta HTTP para limpiar la cookie
     */
    public void logout(HttpServletResponse response) {
        jwtCookieManager.clearJwtCookie(response);
    }
}