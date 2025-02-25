package com.germogli.backend.domain.user.repository;

import com.germogli.backend.domain.user.dto.UpdateUserRoleDTO;
import com.germogli.backend.exception.ExceptionHandlerUtil;
import com.germogli.backend.exception.UserNotFoundException;
import jakarta.persistence.*;
import org.springframework.stereotype.Repository;

@Repository
public class RoleRepositoryImpl implements RoleRepositoryCustom{

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void updateUserRoleSP(UpdateUserRoleDTO updateUserRoleDTO) {
        try {
            // se crea la consulta del SP
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_update_user_role");

            // se registran los parámetros del SP
            query.registerStoredProcedureParameter("p_user_id", Integer.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("p_role_id", Integer.class, ParameterMode.IN);

            // se asignan los valores del DTO a cada parámetro
            query.setParameter("p_user_id", updateUserRoleDTO.getUserId());
            query.setParameter("p_role_id", updateUserRoleDTO.getRoleId());

            // se ejecuta el SP
            query.execute();
        } catch (NoResultException e) {
            throw new UserNotFoundException("No se encontró un usuario con ID: " + updateUserRoleDTO.getUserId());
        } catch (Exception e) {
            throw ExceptionHandlerUtil.handleException(this.getClass(), e, "Error al actualizar el rol del usuario");
        }
    }

}
