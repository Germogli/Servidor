package com.germogli.backend.user.application.service;

import com.germogli.backend.user.application.dto.DeleteUserDTO;
import com.germogli.backend.user.application.dto.GetUserByEmailDTO;
import com.germogli.backend.user.application.dto.UpdateUserInfoDTO;
import com.germogli.backend.user.domain.repository.UserRepository;
import com.germogli.backend.infraestructure.persistence.persistenceUser.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service // Marca esta clase como un servicio que contiene la lógica de negocio
public class UserService {

    // Inyección del repositorio que gestiona las operaciones de usuario.
    private final UserRepository userRepository;

    // Constructor para la inyección de dependencias, que asegura que el repositorio se pasa al servicio.
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional // Garantiza que la operación de actualización se realice de manera atómica.
    //Esto significa que todo el bloque de código dentro del método se tratará como una única unidad de trabajo
    public void updateUserInfo(UpdateUserInfoDTO updateUserInfoDTO) {
        userRepository.updateUserInfoSP(updateUserInfoDTO);
    }

    @Transactional
    public void deleteUser(DeleteUserDTO deleteUserDTO) {
        userRepository.deleteUserSP(deleteUserDTO);
    }

    @Transactional
    public User getUserByEmail(GetUserByEmailDTO getUserByEmailDTO) {
        return userRepository.getUserByEmailSP(getUserByEmailDTO);
    }
}
