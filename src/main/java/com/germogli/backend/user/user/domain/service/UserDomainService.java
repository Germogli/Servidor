package com.germogli.backend.user.user.domain.service;

import com.germogli.backend.authentication.domain.model.UserDomain;
import com.germogli.backend.common.exception.ResourceNotFoundException;
import com.germogli.backend.common.exception.UserNotFoundException;
import com.germogli.backend.user.user.application.dto.DeleteUserDTO;
import com.germogli.backend.user.user.application.dto.GetUserByUsernameDTO;
import com.germogli.backend.user.user.application.dto.UpdateUserInfoDTO;
import com.germogli.backend.user.user.domain.repository.UserDomainRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserDomainService {
    private final UserDomainRepository userRepository;

    /**
     * Actualiza la información de un usuario utilizando el procedimiento almacenado
     * @param updateUserInfoDTO DTO con la información a actualizar
     */
    public void updateUserInfo(UpdateUserInfoDTO updateUserInfoDTO) {
        try {
            // Extrae el usuario autenticado del contexto de seguridad
            UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String username = userDetails.getUsername();

            // Crea un objeto UserDomain con el username para pasarlo al repositorio
            UserDomain userDomainForSearch = UserDomain.builder()
                    .username(username)
                    .build();

            // Obtiene el usuario autenticado completo
            UserDomain authenticatedUser = userRepository.getUserByUsernameSP(userDomainForSearch);

            if (!authenticatedUser.getId().equals(updateUserInfoDTO.getUserId())) {
                throw new AccessDeniedException("No tienes permiso para actualizar la información de otro usuario");
            }

            // Construye el objeto de dominio a partir del DTO
            UserDomain userDomain = UserDomain.builder()
                    .id(updateUserInfoDTO.getUserId())
                    .username(updateUserInfoDTO.getUsername())
                    .avatar(updateUserInfoDTO.getAvatar())
                    .firstName(updateUserInfoDTO.getFirstName())
                    .lastName(updateUserInfoDTO.getLastName())
                    .description(updateUserInfoDTO.getDescription())
                    .build();

            // Invoca el procedimiento almacenado a través del repositorio
            userRepository.updateUserInfoSP(userDomain);
        } catch (AccessDeniedException e) {
            throw e; // Propaga la excepción de acceso denegado
        } catch (Exception e) {
            throw new ResourceNotFoundException("Error al actualizar la información del usuario: " + e.getMessage());
        }
    }

    /**
     * Elimina un usuario utilizando el procedimiento almacenado
     * @param deleteUserDTO DTO con el ID del usuario a eliminar
     */
    public void deleteUser(DeleteUserDTO deleteUserDTO) {
        try {
            // Construye el objeto de dominio a partir del DTO
            UserDomain userDomain = UserDomain.builder()
                    .id(deleteUserDTO.getUserId())
                    .build();

            // Invoca el procedimiento almacenado a través del repositorio
            userRepository.deleteUserSP(userDomain);
        } catch (Exception e) {
            throw new ResourceNotFoundException("Error al eliminar el usuario: " + e.getMessage());
        }
    }

//    public void deleteUser(DeleteUserDTO deleteUserDTO) {
//        try {
//            // Extrae el usuario autenticado del contexto de seguridad
//            UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//            String username = userDetails.getUsername();
//
//            // Busca el usuario autenticado en el sistema
//            UserDomain authenticatedUser = userRepository.findByUsername(username)
//                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado para el username: " + username));
//
//            // Verifica si el usuario autenticado tiene permiso para eliminar este usuario
//            // Solo el propio usuario o un administrador puede eliminar la cuenta
//            if (!authenticatedUser.getId().equals(deleteUserDTO.getUserId()) &&
//                    !authenticatedUser.getRole().equals(Role.ADMIN)) {
//                throw new AccessDeniedException("No tienes permiso para eliminar este usuario");
//            }
//
//            // Construye el objeto de dominio a partir del DTO
//            UserDomain userDomain = UserDomain.builder()
//                    .id(deleteUserDTO.getUserId())
//                    .build();
//
//            // Invoca el procedimiento almacenado a través del repositorio
//            userRepository.deleteUserSP(userDomain);
//        } catch (AccessDeniedException e) {
//            throw e; // Propaga la excepción de acceso denegado
//        } catch (Exception e) {
//            throw new ResourceNotFoundException("Error al eliminar el usuario: " + e.getMessage());
//        }
//    }

    /**
     * Obtiene un usuario por su email utilizando el procedimiento almacenado
     * @param getUserByUsernameDTO DTO con el email del usuario a buscar
     * @return El objeto UserDomain con la información del usuario
     */
    public UserDomain getUserByUsername(GetUserByUsernameDTO getUserByUsernameDTO) {
        try {
            // Construye el objeto de dominio a partir del DTO
            UserDomain userDomain = UserDomain.builder()
                    .username(getUserByUsernameDTO.getUsername())
                    .build();

            // Invoca el procedimiento almacenado a través del repositorio
            return userRepository.getUserByUsernameSP(userDomain);
        } catch (UserNotFoundException e) {
            throw e; // Propaga la excepción específica de usuario no encontrado
        } catch (Exception e) {
            throw new ResourceNotFoundException("Error al obtener el usuario por email: " + e.getMessage());
        }
    }

    /**
     * Convierte un UserDomain a un DTO de respuesta (puedes crear un ResponseDTO según necesites)
     * @param userDomain objeto de dominio a convertir
     * @return DTO con la información necesaria para el cliente
     */
    public UpdateUserInfoDTO toResponseDTO(UserDomain userDomain) {
        return new UpdateUserInfoDTO(
                userDomain.getId(),
                userDomain.getUsername(),
                userDomain.getAvatar(),
                userDomain.getFirstName(),
                userDomain.getLastName(),
                userDomain.getDescription()
        );
    }
}