package com.germogli.backend.community.thread.infrastructure.repository;

import com.germogli.backend.community.thread.domain.model.ThreadDomain;
import com.germogli.backend.community.thread.domain.repository.ThreadDomainRepository;
import com.germogli.backend.community.thread.infrastructure.entity.ThreadEntity;
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
 * Implementación del repositorio para el dominio Thread utilizando procedimientos almacenados.
 */
@Repository("communityThreadRepository")
@RequiredArgsConstructor
public class ThreadRepository implements ThreadDomainRepository {

    @PersistenceContext
    private final EntityManager entityManager;

    @Override
    @Transactional
    public ThreadDomain saveThread(ThreadDomain thread) {
        if (thread.getId() == null) {
            // Crear hilo mediante sp_create_thread
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_create_thread", ThreadEntity.class);
            query.registerStoredProcedureParameter("p_group_id", Integer.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("p_user_id", Integer.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("p_title", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("p_content", String.class, ParameterMode.IN);
            query.setParameter("p_group_id", thread.getGroupId());
            query.setParameter("p_user_id", thread.getUserId());
            query.setParameter("p_title", thread.getTitle());
            query.setParameter("p_content", thread.getContent());
            query.execute();
            // Se asume que el SP asigna el ID; si es necesario se puede recuperar.
            return thread;
        } else {
            // Actualizar hilo mediante sp_update_thread
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_update_thread");
            query.registerStoredProcedureParameter("p_thread_id", Integer.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("p_title", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("p_content", String.class, ParameterMode.IN);
            query.setParameter("p_thread_id", thread.getId());
            query.setParameter("p_title", thread.getTitle());
            query.setParameter("p_content", thread.getContent());
            query.execute();
            return thread;
        }
    }

    @Override
    public Optional<ThreadDomain> findThreadById(Integer id) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_get_thread_by_id", ThreadEntity.class);
        query.registerStoredProcedureParameter("p_thread_id", Integer.class, ParameterMode.IN);
        query.setParameter("p_thread_id", id);
        query.execute();
        List<ThreadEntity> resultList = query.getResultList();
        if (resultList.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(ThreadDomain.fromEntityStatic(resultList.get(0)));
    }

    @Override
    public List<ThreadDomain> findAllThreads() {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_get_all_threads", ThreadEntity.class);
        query.execute();
        List<ThreadEntity> resultList = query.getResultList();
        return resultList.stream().map(ThreadDomain::fromEntityStatic).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteThreadById(Integer id) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_delete_thread");
        query.registerStoredProcedureParameter("p_thread_id", Integer.class, ParameterMode.IN);
        query.setParameter("p_thread_id", id);
        query.execute();
    }
    @Override
    public List<ThreadDomain> findThreadsByGroupId(Integer groupId) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_get_threads_by_group_id", ThreadEntity.class);
        query.registerStoredProcedureParameter("p_group_id", Integer.class, ParameterMode.IN);
        query.setParameter("p_group_id", groupId);
        query.execute();
        List<ThreadEntity> resultList = query.getResultList();
        return resultList.stream().map(ThreadDomain::fromEntityStatic).collect(Collectors.toList());
    }
    @Override
    public List<ThreadDomain> findThreadsByUserId(Integer userId) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_get_threads_by_user_id", ThreadEntity.class);
        query.registerStoredProcedureParameter("p_user_id", Integer.class, ParameterMode.IN);
        query.setParameter("p_user_id", userId);
        query.execute();
        List<ThreadEntity> resultList = query.getResultList();
        return resultList.stream().map(ThreadDomain::fromEntityStatic).collect(Collectors.toList());
    }
    @Override
    @Transactional
    public List<ThreadDomain> findForumThreads() {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_get_forum_threads", ThreadEntity.class);
        query.execute();
        List<ThreadEntity> resultList = query.getResultList();
        return resultList.stream().map(ThreadDomain::fromEntityStatic).collect(Collectors.toList());
    }
}
