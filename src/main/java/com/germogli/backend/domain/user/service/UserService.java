package com.germogli.backend.domain.user.service;

import com.germogli.backend.domain.user.dto.DeleteUserDTO;
import com.germogli.backend.domain.user.dto.GetUserByEmailDTO;
import com.germogli.backend.domain.user.dto.UpdateUserInfoDTO;
import com.germogli.backend.domain.user.repository.UserRepository;
import com.germogli.backend.infraestructure.persistence.persistenceUser.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    // Inyeccion del repositorio de usuario
    private final UserRepository userRepository;

    // Constructor para inyeccion de dependencias
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // metodos que orquestan las funciones de los usuarios usando SP
    @Transactional
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
