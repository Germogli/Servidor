package com.germogli.backend.user.domain.repository;


import com.germogli.backend.infraestructure.persistence.persistenceUser.entity.User;
import com.germogli.backend.user.application.dto.DeleteUserDTO;
import com.germogli.backend.user.application.dto.GetUserByEmailDTO;
import com.germogli.backend.user.application.dto.UpdateUserInfoDTO;

// Define el contrato para el consumo del SP
public interface UserRepositoryCustom {
    void updateUserInfoSP(UpdateUserInfoDTO updateUserInfoDTO);
    void deleteUserSP(DeleteUserDTO deleteUserDTO);
    User getUserByEmailSP(GetUserByEmailDTO getUserByEmailDTO);
}
