package com.germogli.backend.user.role.infrastructure.repository;

import com.germogli.backend.common.exception.ExceptionHandlerUtil;
import com.germogli.backend.common.exception.UserNotFoundException;
import com.germogli.backend.user.role.domain.model.Role;
import com.germogli.backend.user.role.domain.repository.RoleDomainRepository;
import jakarta.persistence.*;
import org.springframework.stereotype.Repository;

@Repository // Indica que esta clase es un componente de acceso a datos.
public class RoleRepository implements RoleDomainRepository {

    @PersistenceContext // Inyecta el EntityManager para interactuar con la base de datos.
    private EntityManager entityManager;

    @Override
    public void updateUserRole(Integer userId, Role role) {
        try {
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_update_user_role");

            query.registerStoredProcedureParameter("p_user_id", Integer.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("p_role_id", Integer.class, ParameterMode.IN);

            query.setParameter("p_user_id", userId);
            query.setParameter("p_role_id", role.getId());

            query.execute();
        } catch (NoResultException e) {
            throw new UserNotFoundException("No se encontró un usuario con ID: " + userId);
        } catch (Exception e) {
            throw ExceptionHandlerUtil.handleException(this.getClass(), e, "Error al actualizar el rol del usuario");
        }
    }
}
