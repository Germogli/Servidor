package com.germogli.backend.user.user.infrastructure.repository;

import com.germogli.backend.user.user.domain.model.Role;
import com.germogli.backend.user.user.domain.model.User;
import com.germogli.backend.user.user.domain.repository.UserDomainRepository;
import com.germogli.backend.user.user.infrastructure.crud.UserUserCrudRepository;
import com.germogli.backend.user.user.infrastructure.entity.UserEntity;
import com.germogli.backend.common.exception.ExceptionHandlerUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.StoredProcedureQuery;
import jakarta.persistence.ParameterMode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository("userUserRepository")
@RequiredArgsConstructor
public class UserRepository implements UserDomainRepository {

    @PersistenceContext
    private EntityManager entityManager;

    private final UserUserCrudRepository userUserCrudRepository;

    @Override
    public void updateUserInfo(User user) {
        try {
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_update_user_info");
            query.registerStoredProcedureParameter("p_user_id", Integer.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("p_username", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("p_avatar", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("p_first_name", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("p_last_name", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("p_description", String.class, ParameterMode.IN);

            query.setParameter("p_user_id", user.getId());
            query.setParameter("p_username", user.getUsername());
            query.setParameter("p_avatar", user.getAvatar());
            query.setParameter("p_first_name", user.getFirstName());
            query.setParameter("p_last_name", user.getLastName());
            query.setParameter("p_description", user.getDescription());

            query.execute();
        } catch (Exception e) {
            throw ExceptionHandlerUtil.handleException(this.getClass(), e, "Error al actualizar información del usuario");
        }
    }

    @Override
    public void deleteUser(User user) {
        try {
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_delete_user");
            query.registerStoredProcedureParameter("p_user_id", Integer.class, ParameterMode.IN);
            query.setParameter("p_user_id", user.getId());
            query.execute();
        } catch (Exception e) {
            throw ExceptionHandlerUtil.handleException(this.getClass(), e, "Error al eliminar el usuario");
        }
    }

    @Override
    public User getUserByUsername(String username) {
        try {
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_get_user_by_username", UserEntity.class);
            query.registerStoredProcedureParameter("p_username", String.class, ParameterMode.IN);
            query.setParameter("p_username", username);
            query.execute();
            UserEntity entity = (UserEntity) query.getSingleResult();
            return convertToDomain(entity);
        } catch (NoResultException e) {
            throw new RuntimeException("No se encontró un usuario con el nombre de usuario: " + username);
        } catch (Exception e) {
            throw ExceptionHandlerUtil.handleException(this.getClass(), e, "Error al obtener el usuario");
        }
    }

    @Override
    public User getUserById(Integer id) {
        return userUserCrudRepository.findById(id)
                .map(this::convertToDomain)
                .orElseThrow(() -> new RuntimeException("No se encontró un usuario con ID: " + id));
    }

    private User convertToDomain(UserEntity entity) {
        return User.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .email(entity.getEmail())
                .password(entity.getPassword())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .country(entity.getCountry())
                .avatar(entity.getAvatar())
                .description(entity.getDescription())
                .isActive(entity.getIsActive())
                .role(Role.builder()
                        .id(entity.getRole().getId())
                        .roleType(entity.getRole().getRoleType())
                        .build())
                .creationDate(entity.getCreationDate())
                .build();
    }
}
