package com.germogli.backend.domain.user.service;

import com.germogli.backend.domain.user.dto.DeleteUserDTO;
import com.germogli.backend.domain.user.dto.GetUserByEmailDTO;
import com.germogli.backend.domain.user.dto.UpdateUserInfoDTO;
import com.germogli.backend.domain.user.repository.IUserRepository;
import com.germogli.backend.infraestructure.persistence.persistenceUser.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    // Inyeccion del repositorio de usuario
    private final IUserRepository IUserRepository;

    // Constructor para inyeccion de dependencias
    public UserService(IUserRepository IUserRepository) {
        this.IUserRepository = IUserRepository;
    }

    // metodos que orquestan las funciones de los usuarios usando SP
    @Transactional
    public void updateUserInfo(UpdateUserInfoDTO updateUserInfoDTO) {
        IUserRepository.updateUserInfoSP(updateUserInfoDTO);
    }

    @Transactional
    public void deleteUser(DeleteUserDTO deleteUserDTO) {
        IUserRepository.deleteUserSP(deleteUserDTO);
    }

    @Transactional
    public User getUserByEmail(GetUserByEmailDTO getUserByEmailDTO) {
        return IUserRepository.getUserByEmailSP(getUserByEmailDTO);
    }
}
