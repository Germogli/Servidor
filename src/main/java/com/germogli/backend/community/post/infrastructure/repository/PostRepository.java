package com.germogli.backend.community.post.infrastructure.repository;

import com.germogli.backend.community.post.domain.model.PostDomain;
import com.germogli.backend.community.post.domain.repository.PostDomainRepository;
import com.germogli.backend.community.post.infrastructure.entity.PostEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.StoredProcedureQuery;
import jakarta.persistence.ParameterMode;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementación del repositorio de dominio para Post, usando procedimientos almacenados.
 */
@Repository("communityPostRepository")
@RequiredArgsConstructor
public class PostRepository implements PostDomainRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public PostDomain save(PostDomain post) {
        if (post.getId() == null) {
            // Crear nuevo post mediante sp_create_post
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_create_post");
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

    @Override
    public Optional<PostDomain> findById(Integer id) {
        // Consulta mediante sp_get_post_by_id
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_get_post_by_id", PostEntity.class);
        query.registerStoredProcedureParameter("p_post_id", Integer.class, ParameterMode.IN);
        query.setParameter("p_post_id", id);
        query.execute();
        List<PostEntity> resultList = query.getResultList();
        if (resultList.isEmpty()) {
            return Optional.empty();
        }
        PostEntity entity = resultList.get(0);
        return Optional.of(PostDomain.fromEntity(entity));
    }

    @Override
    public List<PostDomain> findAll() {
        // Consulta mediante sp_get_all_posts
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_get_all_posts", PostEntity.class);
        query.execute();
        List<PostEntity> resultList = query.getResultList();
        return resultList.stream().map(PostDomain::fromEntity).collect(Collectors.toList());
    }

    @Override
    public void deleteById(Integer id) {
        // Eliminación mediante sp_delete_post
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_delete_post");
        query.registerStoredProcedureParameter("p_post_id", Integer.class, ParameterMode.IN);
        query.setParameter("p_post_id", id);
        query.execute();
    }
}
