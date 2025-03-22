package com.germogli.backend.community.thread.domain.repository;

import com.germogli.backend.community.thread.domain.model.ThreadDomain;
import com.germogli.backend.community.thread.domain.model.ThreadReplyDomain;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz para operaciones de persistencia de hilos y respuestas en el módulo thread.
 */
public interface ThreadDomainRepository {
    // Métodos para hilos (threads)
    ThreadDomain saveThread(ThreadDomain thread);
    Optional<ThreadDomain> findThreadById(Integer id);
    List<ThreadDomain> findAllThreads();
    void deleteThreadById(Integer id);

    // Métodos para respuestas (thread replies)
    ThreadReplyDomain saveThreadReply(ThreadReplyDomain reply);
    Optional<ThreadReplyDomain> findThreadReplyById(Integer id);
    List<ThreadReplyDomain> findAllRepliesByThreadId(Integer threadId);
    void deleteThreadReplyById(Integer id);
}
