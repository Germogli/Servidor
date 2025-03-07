package com.germogli.backend.user.role.infrastructure.repository;


import com.germogli.backend.common.exception.ExceptionHandlerUtil;
import com.germogli.backend.common.exception.UserNotFoundException;
import com.germogli.backend.user.role.application.dto.UpdateUserRoleDTO;
import jakarta.persistence.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository // Indica que esta clase es un componente de acceso a datos.
public class RoleRepository implements com.germogli.backend.user.role.domain.repository.RoleDomainRepository {

    @PersistenceContext // Inyecta el EntityManager para interactuar con la base de datos.
    private EntityManager entityManager;

    @Override
    @Transactional
    public void updateUserRoleSP(UpdateUserRoleDTO updateUserRoleDTO) {
        try {
            // Se crea la consulta para invocar el procedimiento almacenado (SP).
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_update_user_role");

            // Se registran los parámetros que espera el SP
            // ParameterMode.IN significa que estos valores se envían al SP.
            query.registerStoredProcedureParameter("p_user_id", Integer.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("p_role_id", Integer.class, ParameterMode.IN);

            // se asignan los valores del DTO a cada parametro
            // Se pasan los valores desde el DTO correspondiente a los parámetros del SP.
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
