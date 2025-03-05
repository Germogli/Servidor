package com.germogli.backend.user.domain.repository;


import com.germogli.backend.exception.ExceptionHandlerUtil;
import com.germogli.backend.exception.UserNotFoundException;
import com.germogli.backend.infraestructure.persistence.persistenceUser.entity.User;
import com.germogli.backend.user.application.dto.DeleteUserDTO;
import com.germogli.backend.user.application.dto.GetUserByEmailDTO;
import com.germogli.backend.user.application.dto.UpdateUserInfoDTO;
import jakarta.persistence.*;
import org.springframework.stereotype.Repository;

@Repository // Indica que esta clase es un componente de acceso a datos.
public class UserRepositoryImpl implements UserRepositoryCustom{

    @PersistenceContext // Inyecta el EntityManager para interactuar con la base de datos.
    private EntityManager entityManager;

    @Override
    public void updateUserInfoSP(UpdateUserInfoDTO updateUserInfoDTO) {
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
            query.setParameter("p_user_id", updateUserInfoDTO.getUserId());
            query.setParameter("p_username", updateUserInfoDTO.getUsername());
            query.setParameter("p_avatar", updateUserInfoDTO.getAvatar());
            query.setParameter("p_first_name", updateUserInfoDTO.getFirstName());
            query.setParameter("p_last_name", updateUserInfoDTO.getLastName());
            query.setParameter("p_description", updateUserInfoDTO.getDescription());

            // ejecutar SP
            query.execute();
        } catch (Exception e) {
            throw ExceptionHandlerUtil.handleException(this.getClass(), e, "Error al actualizar informacion del usuario");
        }
    }

    @Override
    public void deleteUserSP(DeleteUserDTO deleteUserDTO) {
        try {
            // Se crea la consulta para invocar el procedimiento almacenado (SP).
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_delete_user");

            // Se registran los parámetros que espera el SP
            // ParameterMode.IN significa que estos valores se envían al SP.
            query.registerStoredProcedureParameter("p_user_id", Integer.class, ParameterMode.IN);

            // se asignan los valores del DTO a cada parametro
            // Se pasan los valores desde el DTO correspondiente a los parámetros del SP.
            query.setParameter("p_user_id", deleteUserDTO.getUserId());

            query.execute();
        } catch (Exception e) {
            throw ExceptionHandlerUtil.handleException(this.getClass(), e, "Error al eliminar usuario");
        }
    }

    @Override
    public User getUserByEmailSP(GetUserByEmailDTO getUserByEmailDTO) {
        try {
            // Se crea la consulta para invocar el procedimiento almacenado (SP).
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_get_user_by_email", User.class);

            // Se registran los parámetros que espera el SP
            // ParameterMode.IN significa que estos valores se envían al SP.
            query.registerStoredProcedureParameter("p_email", String.class, ParameterMode.IN);

            // se asignan los valores del DTO a cada parametro
            // Se pasan los valores desde el DTO correspondiente a los parámetros del SP.
            query.setParameter("p_email", getUserByEmailDTO.getEmail());

            // Ejecuta el SP y obtiene el resultado
            return (User) query.getSingleResult();
        } catch (NoResultException e) {
            throw new UserNotFoundException("No se encontró un usuario con el correo: " + getUserByEmailDTO.getEmail());
        } catch (Exception e) {
            throw ExceptionHandlerUtil.handleException(this.getClass(), e, "Error al obtener usuario");
        }
    }
}
