package com.germogli.backend.user.user.infrastructure.repository;


import com.germogli.backend.authentication.domain.model.UserDomain;
import com.germogli.backend.authentication.infrastructure.entity.UserEntity;
import com.germogli.backend.common.exception.ExceptionHandlerUtil;
import com.germogli.backend.common.exception.UserNotFoundException;

import com.germogli.backend.user.user.domain.repository.UserDomainRepository;
import jakarta.persistence.*;
import org.springframework.stereotype.Repository;

@Repository // Indica que esta clase es un componente de acceso a datos.
public class SPUserRepository implements UserDomainRepository {

    @PersistenceContext // Inyecta el EntityManager para interactuar con la base de datos.
    private EntityManager entityManager;

    @Override
    public void updateUserInfoSP(UserDomain user) {
        try {
            // Se crea la consulta para invocar el procedimiento almacenado (SP).
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_update_user_info");

            // Se registran los parámetros que espera el SP
            // ParameterMode.IN significa que estos valores se envían al SP.
            query.registerStoredProcedureParameter("p_user_id", Integer.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("p_username", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("p_avatar", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("p_first_name", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("p_last_name", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("p_description", String.class, ParameterMode.IN);

            // se asignan los valores del DTO a cada parametro
            // Se pasan los valores desde el DTO correspondiente a los parámetros del SP.
            query.setParameter("p_user_id", user.getId());
            query.setParameter("p_username", user.getUsername());
            query.setParameter("p_avatar", user.getAvatar());
            query.setParameter("p_first_name", user.getFirstName());
            query.setParameter("p_last_name", user.getLastName());
            query.setParameter("p_description", user.getDescription());

            // ejecutar SP
            query.execute();
        } catch (Exception e) {
            throw ExceptionHandlerUtil.handleException(this.getClass(), e, "Error al actualizar informacion del usuario");
        }
    }

    @Override
    public void deleteUserSP(UserDomain user) {
        try {
            // Se crea la consulta para invocar el procedimiento almacenado (SP).
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_delete_user");

            // Se registran los parámetros que espera el SP
            // ParameterMode.IN significa que estos valores se envían al SP.
            query.registerStoredProcedureParameter("p_user_id", Integer.class, ParameterMode.IN);

            // se asignan los valores del DTO a cada parametro
            // Se pasan los valores desde el DTO correspondiente a los parámetros del SP.
            query.setParameter("p_user_id", user.getId());

            query.execute();
        } catch (Exception e) {
            throw ExceptionHandlerUtil.handleException(this.getClass(), e, "Error al eliminar usuario");
        }
    }

    @Override
    public UserDomain getUserByUsernameSP(UserDomain user) {
        try {
            // Se crea la consulta para invocar el procedimiento almacenado (SP).
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_get_user_by_username", UserEntity.class);

            // Se registran los parámetros que espera el SP
            // ParameterMode.IN significa que estos valores se envían al SP.
            query.registerStoredProcedureParameter("p_username", String.class, ParameterMode.IN);

            // se asignan los valores del DTO a cada parametro
            // Se pasan los valores desde el DTO correspondiente a los parámetros del SP.
            query.setParameter("p_username", user.getUsername());

            // Ejecuta el SP y obtiene el resultado
            return (UserDomain) query.getSingleResult();
        } catch (NoResultException e) {
            throw new UserNotFoundException("No se encontró un usuario con el nombre de usuario: " + user.getUsername());
        } catch (Exception e) {
            throw ExceptionHandlerUtil.handleException(this.getClass(), e, "Error al obtener usuario");
        }
    }
}
