package com.germogli.backend.user.user.domain.service;

import com.germogli.backend.user.user.application.dto.DeleteUserDTO;
import com.germogli.backend.user.user.application.dto.GetUserByUsernameDTO;
import com.germogli.backend.user.user.application.dto.UpdateUserInfoDTO;
import com.germogli.backend.user.user.application.dto.UserResponseDTO;
import com.germogli.backend.user.user.domain.model.User;
import com.germogli.backend.common.exception.ResourceNotFoundException;
import com.germogli.backend.common.exception.UserNotFoundException;
import com.germogli.backend.user.user.domain.repository.UserDomainRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class UserDomainService {

    private final @Qualifier("userUserRepository")  UserDomainRepository userRepository;

    /**
     * Obtiene un usuario por su ID.
     *
     * @param userId ID del usuario a buscar.
     * @return Usuario encontrado.
     * @throws ResourceNotFoundException si no se encuentra el usuario.
     */
    public User getUserById(Integer userId) {
        User user = userRepository.getUserById(userId);
        if (user == null) {
            throw new UserNotFoundException("No se encontró un usuario con el ID: " + userId);
        }
        return user;
    }

    public void updateUserInfo(UpdateUserInfoDTO dto) {
        UserDetails authUserDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String authUsername = authUserDetails.getUsername();

        User authenticatedUser = userRepository.getUserByUsername(authUsername);
        if (authenticatedUser == null) {
            throw new UserNotFoundException("Usuario no encontrado: " + authUsername);
        }

        // Verificar si el usuario es administrador
        boolean isAdmin = authenticatedUser.getRole() != null &&
                authenticatedUser.getRole().getRoleType().equalsIgnoreCase("ADMINISTRADOR");

        // Permitir la actualización si:
        // 1. Es el mismo usuario actualizando su propia información, O
        // 2. Es un administrador actualizando información de otro usuario
        if (!authenticatedUser.getId().equals(dto.getUserId()) && !isAdmin) {
            throw new AccessDeniedException("No tienes permiso para actualizar la información de otro usuario");
        }

        // Verificar que el usuario a actualizar existe
        User userToUpdate = userRepository.getUserById(dto.getUserId());
        if (userToUpdate == null) {
            throw new UserNotFoundException("No se encontró un usuario con ID: " + dto.getUserId());
        }

        User updatedUser = User.builder()
                .id(dto.getUserId())
                .username(dto.getUsername())
                .avatar(dto.getAvatar())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .description(dto.getDescription())
                .build();
        userRepository.updateUserInfo(updatedUser);
    }

    public void deleteUser(DeleteUserDTO dto) {
        // Obtener el usuario autenticado
        UserDetails authUserDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String authUsername = authUserDetails.getUsername();

        User authenticatedUser = userRepository.getUserByUsername(authUsername);
        if (authenticatedUser == null) {
            throw new UserNotFoundException("Usuario autenticado no encontrado: " + authUsername);
        }

        // Verificar que el usuario tenga rol de administrador
        boolean isAdmin = authenticatedUser.getRole() != null &&
                authenticatedUser.getRole().getRoleType().equalsIgnoreCase("ADMINISTRADOR");

        if (!isAdmin) {
            throw new AccessDeniedException("No tiene permisos para eliminar usuarios. Se requiere rol de administrador.");
        }

        // Verificar que el usuario a eliminar existe
        User userToDelete = userRepository.getUserById(dto.getUserId());
        if (userToDelete == null) {
            throw new ResourceNotFoundException("Usuario no encontrado con ID: " + dto.getUserId());
        }

        // Prevenir que un administrador se elimine a sí mismo
        if (authenticatedUser.getId().equals(dto.getUserId())) {
            throw new AccessDeniedException("No puedes eliminar tu propia cuenta.");
        }

        userRepository.deleteUser(userToDelete);
    }

    public User getUserByUsername(GetUserByUsernameDTO dto) {
        User user = userRepository.getUserByUsername(dto.getUsername());
        if (user == null) {
            throw new UserNotFoundException("No se encontró un usuario con el nombre: " + dto.getUsername());
        }
        return user;
    }

    public UpdateUserInfoDTO toResponseDTO(User user) {
        return new UpdateUserInfoDTO(
                user.getId(),
                user.getUsername(),
                user.getAvatar(),
                user.getFirstName(),
                user.getLastName(),
                user.getDescription()
        );
    }

    /**
     * Obtiene todos los usuarios del sistema.
     * IMPORTANTE: Solo usuarios con rol ADMINISTRADOR pueden acceder a esta funcionalidad.
     *
     * @return Lista de todos los usuarios del sistema.
     * @throws AccessDeniedException si el usuario no tiene permisos de administrador.
     */
    public List<User> getAllUsers() {
        // Obtener el usuario autenticado del contexto de seguridad
        UserDetails authUserDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String authUsername = authUserDetails.getUsername();

        // Buscar el usuario completo para verificar su rol
        User authenticatedUser = userRepository.getUserByUsername(authUsername);
        if (authenticatedUser == null) {
            throw new UserNotFoundException("Usuario autenticado no encontrado: " + authUsername);
        }

        // Verificar que el usuario tenga rol de administrador
        boolean isAdmin = authenticatedUser.getRole() != null &&
                authenticatedUser.getRole().getRoleType().equalsIgnoreCase("ADMINISTRADOR");

        if (!isAdmin) {
            throw new AccessDeniedException("No tiene permisos para acceder a la lista de usuarios. Se requiere rol de administrador.");
        }

        // Si es administrador, proceder a obtener todos los usuarios
        return userRepository.getAllUsers();
    }

    /**
     * Convierte un objeto User de dominio a UserResponseDTO para el listado.
     *
     * @param user Usuario de dominio.
     * @return DTO de respuesta con información del usuario.
     */
    public UserResponseDTO toUserResponseDTO(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .country(user.getCountry())
                .avatar(user.getAvatar())
                .description(user.getDescription())
                .isActive(user.getIsActive())
                .roleType(user.getRole() != null ? user.getRole().getRoleType() : null)
                .creationDate(user.getCreationDate())
                .build();
    }

    /**
     * Convierte una lista de usuarios de dominio a una lista de DTOs de respuesta.
     *
     * @param users Lista de usuarios de dominio.
     * @return Lista de DTOs de respuesta.
     */
    public List<UserResponseDTO> toUserResponseDTOList(List<User> users) {
        return users.stream()
                .map(this::toUserResponseDTO)
                .collect(Collectors.toList());
    }
}
