package com.germogli.backend.community.thread.domain.repository;

import com.germogli.backend.community.thread.domain.model.ThreadDomain;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz para operaciones de persistencia de hilos en el módulo thread.
 */
public interface ThreadDomainRepository {
    ThreadDomain saveThread(ThreadDomain thread);
    Optional<ThreadDomain> findThreadById(Integer id);
    List<ThreadDomain> findAllThreads();
    void deleteThreadById(Integer id);
    List<ThreadDomain> findThreadsByGroupId(Integer groupId);
    List<ThreadDomain> findThreadsByUserId(Integer userId);
    List<ThreadDomain> findForumThreads();
}
