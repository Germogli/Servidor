package com.germogli.backend.authentication.web.config;

import com.germogli.backend.authentication.infrastructure.crud.AuthenticationUserCrudRepository;
import com.germogli.backend.authentication.infrastructure.repository.UserRepository;
import com.germogli.backend.authentication.domain.repository.UserDomainRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuración de beans para el módulo de autenticación.
 * Define el AuthenticationManager, el AuthenticationProvider, el PasswordEncoder y el UserDetailsService.
 */
@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {
    // Repositorio CRUD para usuarios autenticados.
    private final AuthenticationUserCrudRepository authenticationUserCrudRepository;

    /**
     * Configura el AuthenticationManager a partir de la configuración de autenticación.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Configura el AuthenticationProvider usando DaoAuthenticationProvider.
     * Se inyecta el UserDetailsService y el PasswordEncoder.
     */
    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    /**
     * Bean para codificar contraseñas usando BCrypt.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Define el UserDetailsService utilizando el repositorio de usuarios.
     *
     * @param userRepository Repositorio para usuarios.
     * @return UserDetailsService.
     */
    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return username -> userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .toUserDetails();
    }

    /**
     * Bean para el repositorio de dominio de usuarios.
     * Envuelve el CRUD de autenticación.
     *
     * @param crudRepo Repositorio CRUD.
     * @return UserDomainRepository.
     */
    @Bean
    public UserDomainRepository userDomainRepository(AuthenticationUserCrudRepository crudRepo) {
        return new UserRepository(crudRepo);
    }
}
