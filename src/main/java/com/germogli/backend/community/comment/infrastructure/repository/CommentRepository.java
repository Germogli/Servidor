package com.germogli.backend.community.comment.infrastructure.repository;

import com.germogli.backend.community.comment.domain.model.CommentDomain;
import com.germogli.backend.community.comment.domain.repository.CommentDomainRepository;
import com.germogli.backend.community.comment.infrastructure.entity.CommentEntity;
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
 * Implementación del repositorio para el dominio Comment utilizando procedimientos almacenados.
 */
@Repository("communityCommentRepository")
@RequiredArgsConstructor
public class CommentRepository implements CommentDomainRepository {

    @PersistenceContext
    private final EntityManager entityManager;

    /**
     * Guarda o actualiza un comentario usando procedimientos almacenados.
     * Si el comentario es nuevo (ID nulo), se crea con sp_create_comment;
     * si ya existe, se actualiza con sp_update_comment.
     */
    @Override
    @Transactional
    public CommentDomain save(CommentDomain comment) {
        if (comment.getId() == null) {
            // Crear comentario mediante sp_create_comment
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_create_comment", CommentEntity.class);
            query.registerStoredProcedureParameter("p_post_id", Integer.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("p_user_id", Integer.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("p_content", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("p_thread_id", Integer.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("p_group_id", Integer.class, ParameterMode.IN);
            query.setParameter("p_post_id", comment.getPostId());
            query.setParameter("p_user_id", comment.getUserId());
            query.setParameter("p_content", comment.getContent());
            query.setParameter("p_thread_id", comment.getThreadId());
            query.setParameter("p_group_id", comment.getGroupId());
            query.execute();
            return comment;
        } else {
            // Actualizar comentario mediante sp_update_comment
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_update_comment", CommentEntity.class);
            query.registerStoredProcedureParameter("p_comment_id", Integer.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("p_content", String.class, ParameterMode.IN);
            query.setParameter("p_comment_id", comment.getId());
            query.setParameter("p_content", comment.getContent());
            query.execute();
            return comment;
        }
    }

    /**
     * Busca un comentario por su ID usando sp_get_comment_by_id.
     */
    @Override
    public Optional<CommentDomain> findById(Integer id) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_get_comment_by_id", CommentEntity.class);
        query.registerStoredProcedureParameter("p_comment_id", Integer.class, ParameterMode.IN);
        query.setParameter("p_comment_id", id);
        query.execute();
        List<CommentEntity> resultList = query.getResultList();
        if (resultList.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(CommentDomain.fromEntityStatic(resultList.get(0)));
    }

    /**
     * Obtiene todos los comentarios usando sp_get_all_comments.
     */
    @Override
    public List<CommentDomain> findAll() {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_get_all_comments", CommentEntity.class);
        query.execute();
        List<CommentEntity> resultList = query.getResultList();
        return resultList.stream().map(CommentDomain::fromEntityStatic).collect(Collectors.toList());
    }

    /**
     * Elimina un comentario por su ID usando sp_delete_comment.
     */
    @Override
    @Transactional
    public void deleteById(Integer id) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_delete_comment");
        query.registerStoredProcedureParameter("p_comment_id", Integer.class, ParameterMode.IN);
        query.setParameter("p_comment_id", id);
        query.execute();
    }
}
