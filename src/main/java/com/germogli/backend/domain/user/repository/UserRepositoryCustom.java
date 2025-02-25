package com.germogli.backend.domain.user.repository;

import com.germogli.backend.domain.user.dto.DeleteUserDTO;
import com.germogli.backend.domain.user.dto.GetUserByEmailDTO;
import com.germogli.backend.domain.user.dto.UpdateUserInfoDTO;
import com.germogli.backend.infraestructure.persistence.persistenceUser.entity.User;

public interface UserRepositoryCustom {
    void updateUserInfoSP(UpdateUserInfoDTO updateUserInfoDTO);
    void deleteUserSP(DeleteUserDTO deleteUserDTO);
    User getUserByEmailSP(GetUserByEmailDTO getUserByEmailDTO);
}
