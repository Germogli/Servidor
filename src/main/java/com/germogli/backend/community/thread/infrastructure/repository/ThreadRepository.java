package com.germogli.backend.community.thread.infrastructure.repository;

import com.germogli.backend.community.thread.domain.model.ThreadDomain;
import com.germogli.backend.community.thread.domain.model.ThreadReplyDomain;
import com.germogli.backend.community.thread.domain.repository.ThreadDomainRepository;
import com.germogli.backend.community.thread.infrastructure.entity.ThreadEntity;
import com.germogli.backend.community.thread.infrastructure.entity.ThreadReplyEntity;
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

    // Métodos para hilos (threads)

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
            // Se asume que el SP asigna el ID (si es necesario, se puede recuperar)
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
        // Obtener hilo mediante sp_get_thread_by_id (se asume que el SP existe)
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
        // Obtener todos los hilos mediante sp_get_all_threads (se asume que el SP existe)
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_get_all_threads", ThreadEntity.class);
        query.execute();
        List<ThreadEntity> resultList = query.getResultList();
        return resultList.stream().map(ThreadDomain::fromEntityStatic).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteThreadById(Integer id) {
        // Eliminar hilo mediante sp_delete_thread (se asume que el SP existe)
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_delete_thread");
        query.registerStoredProcedureParameter("p_thread_id", Integer.class, ParameterMode.IN);
        query.setParameter("p_thread_id", id);
        query.execute();
    }

    // Métodos para respuestas (thread replies)

    @Override
    @Transactional
    public ThreadReplyDomain saveThreadReply(ThreadReplyDomain reply) {
        if (reply.getId() == null) {
            // Crear respuesta mediante sp_create_thread_reply
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_create_thread_reply", ThreadReplyEntity.class);
            query.registerStoredProcedureParameter("p_thread_id", Integer.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("p_user_id", Integer.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("p_content", String.class, ParameterMode.IN);
            query.setParameter("p_thread_id", reply.getThreadId());
            query.setParameter("p_user_id", reply.getUserId());
            query.setParameter("p_content", reply.getContent());
            query.execute();
            return reply;
        } else {
            // Actualizar respuesta mediante sp_update_thread_reply (se asume que dicho SP existe)
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_update_thread_reply");
            query.registerStoredProcedureParameter("p_reply_id", Integer.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("p_content", String.class, ParameterMode.IN);
            query.setParameter("p_reply_id", reply.getId());
            query.setParameter("p_content", reply.getContent());
            query.execute();
            return reply;
        }
    }

    @Override
    public Optional<ThreadReplyDomain> findThreadReplyById(Integer id) {
        // Obtener respuesta mediante sp_get_thread_reply_by_id (se asume que el SP existe)
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_get_thread_reply_by_id", ThreadReplyEntity.class);
        query.registerStoredProcedureParameter("p_reply_id", Integer.class, ParameterMode.IN);
        query.setParameter("p_reply_id", id);
        query.execute();
        List<ThreadReplyEntity> resultList = query.getResultList();
        if (resultList.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(ThreadReplyDomain.fromEntityStatic(resultList.get(0)));
    }

    @Override
    public List<ThreadReplyDomain> findAllRepliesByThreadId(Integer threadId) {
        // Obtener respuestas mediante sp_get_all_replies_by_thread (se asume que el SP existe)
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_get_all_replies_by_thread", ThreadReplyEntity.class);
        query.registerStoredProcedureParameter("p_thread_id", Integer.class, ParameterMode.IN);
        query.setParameter("p_thread_id", threadId);
        query.execute();
        List<ThreadReplyEntity> resultList = query.getResultList();
        return resultList.stream().map(ThreadReplyDomain::fromEntityStatic).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteThreadReplyById(Integer id) {
        // Eliminar respuesta mediante sp_delete_thread_reply (se asume que el SP existe)
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_delete_thread_reply");
        query.registerStoredProcedureParameter("p_reply_id", Integer.class, ParameterMode.IN);
        query.setParameter("p_reply_id", id);
        query.execute();
    }
}
