package com.germogli.backend.community.post.infrastructure.repository;

import com.germogli.backend.community.post.domain.model.PostDomain;
import com.germogli.backend.community.post.domain.repository.PostDomainRepository;
import com.germogli.backend.community.post.infrastructure.entity.PostEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.StoredProcedureQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementación del repositorio para el dominio Post utilizando procedimientos almacenados.
 */
@Repository("communityPostRepository")
@RequiredArgsConstructor
public class PostRepository implements PostDomainRepository {

    @PersistenceContext
    private final EntityManager entityManager;

    /**
     * Guarda o actualiza una publicación.
     * Si el post es nuevo (ID nulo), se crea mediante sp_create_post y se recupera el ID generado.
     * Si el post ya existe, se actualiza mediante sp_update_post.
     */
    @Override
    @Transactional
    public PostDomain save(PostDomain post) {
        if (post.getId() == null) {
            // Crear post mediante sp_create_post
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_create_post", PostEntity.class);
            query.registerStoredProcedureParameter("p_user_id", Integer.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("p_post_type", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("p_content", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("p_multimedia_content", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("p_group_id", Integer.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("p_thread_id", Integer.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("p_post_id", Integer.class, ParameterMode.OUT);
            query.setParameter("p_user_id", post.getUserId());
            query.setParameter("p_post_type", post.getPostType());
            query.setParameter("p_content", post.getContent());
            query.setParameter("p_multimedia_content", post.getMultimediaContent());
            query.setParameter("p_group_id", post.getGroupId());
            query.setParameter("p_thread_id", post.getThreadId());
            query.execute();
            Integer generatedId = (Integer) query.getOutputParameterValue("p_post_id");
            post.setId(generatedId);
            return post;
        } else {
            // Actualizar post mediante sp_update_post
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_update_post");
            query.registerStoredProcedureParameter("p_post_id", Integer.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("p_post_type", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("p_content", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("p_multimedia_content", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("p_group_id", Integer.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("p_thread_id", Integer.class, ParameterMode.IN);
            query.setParameter("p_post_id", post.getId());
            query.setParameter("p_post_type", post.getPostType());
            query.setParameter("p_content", post.getContent());
            query.setParameter("p_multimedia_content", post.getMultimediaContent());
            query.setParameter("p_group_id", post.getGroupId());
            query.setParameter("p_thread_id", post.getThreadId());
            query.execute();
            return post;
        }
    }

    /**
     * Obtiene una publicación por su ID mediante sp_get_post_by_id.
     */
    @Override
    public Optional<PostDomain> findById(Integer id) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_get_post_by_id", PostEntity.class);
        query.registerStoredProcedureParameter("p_post_id", Integer.class, ParameterMode.IN);
        query.setParameter("p_post_id", id);
        query.execute();
        List<PostEntity> resultList = query.getResultList();
        if (resultList.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(PostDomain.fromEntityStatic(resultList.get(0)));
    }

    /**
     * Obtiene todas las publicaciones mediante sp_get_all_posts.
     */
    @Override
    public List<PostDomain> findAll() {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_get_all_posts", PostEntity.class);
        query.execute();
        List<PostEntity> resultList = query.getResultList();
        return resultList.stream().map(PostDomain::fromEntityStatic).collect(Collectors.toList());
    }

    /**
     * Elimina una publicación mediante sp_delete_post.
     */
    @Override
    @Transactional
    public void deleteById(Integer id) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_delete_post");
        query.registerStoredProcedureParameter("p_post_id", Integer.class, ParameterMode.IN);
        query.setParameter("p_post_id", id);
        query.execute();
    }
    /**
     * Obtiene el userId (autor) de un post mediante sp_get_post_owner.
     *
     * @param postId ID del post del cual se desea obtener el autor.
     * @return El ID del usuario que creó el post.
     *
     */
    @Override
    @Transactional
    public Integer findOwnerIdByPostId(Integer postId) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_get_post_owner");
        query.registerStoredProcedureParameter("p_post_id", Integer.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_user_id", Integer.class, ParameterMode.OUT);

        query.setParameter("p_post_id", postId);
        query.execute();

        Integer ownerId = (Integer) query.getOutputParameterValue("p_user_id");

        return ownerId;
    }

    /**
     * Obtiene las publicaciones de un grupo mediante sp_get_posts_by_group_id.
     * @param groupId
     * @return
     */
    @Override
    @Transactional
    public List<PostDomain> findByGroupId(Integer groupId) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_get_posts_by_group_id", PostEntity.class);
        query.registerStoredProcedureParameter("p_group_id", Integer.class, ParameterMode.IN);
        query.setParameter("p_group_id", groupId);
        query.execute();
        List<PostEntity> resultList = query.getResultList();
        return resultList.stream().map(PostDomain::fromEntityStatic).collect(Collectors.toList());
    }
    @Override
    @Transactional
    public List<PostDomain> findByUserId(Integer userId) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_get_posts_by_user_id", PostEntity.class);
        query.registerStoredProcedureParameter("p_user_id", Integer.class, ParameterMode.IN);
        query.setParameter("p_user_id", userId);
        query.execute();
        List<PostEntity> resultList = query.getResultList();
        return resultList.stream().map(PostDomain::fromEntityStatic).collect(Collectors.toList());
    }
}
