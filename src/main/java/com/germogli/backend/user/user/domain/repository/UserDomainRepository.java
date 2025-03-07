package com.germogli.backend.user.user.domain.repository;


import com.germogli.backend.authentication.domain.model.UserDomain;

// Define el contrato para el consumo del SP
public interface UserDomainRepository {
    void updateUserInfoSP(UserDomain user);
    void deleteUserSP(UserDomain user);
    UserDomain getUserByUsernameSP(UserDomain user);
}
