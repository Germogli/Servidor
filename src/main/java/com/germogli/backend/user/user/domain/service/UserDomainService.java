package com.germogli.backend.user.user.domain.service;

import com.germogli.backend.user.user.application.dto.DeleteUserDTO;
import com.germogli.backend.user.user.application.dto.GetUserByUsernameDTO;
import com.germogli.backend.user.user.application.dto.UpdateUserInfoDTO;
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
        if (!authenticatedUser.getId().equals(dto.getUserId())) {
            throw new AccessDeniedException("No tienes permiso para actualizar la información de otro usuario");
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
        // Usamos getUserById para obtener el usuario correctamente mediante su ID
        User user = userRepository.getUserById(dto.getUserId());
        if (user == null) {
            throw new ResourceNotFoundException("Usuario no encontrado con ID: " + dto.getUserId());
        }
        userRepository.deleteUser(user);
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
}
