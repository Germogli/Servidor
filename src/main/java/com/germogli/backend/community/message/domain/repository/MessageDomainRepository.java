package com.germogli.backend.community.message.domain.repository;

import com.germogli.backend.community.message.domain.model.MessageDomain;
import java.util.List;
import java.util.Optional;

/**
 * Define las operaciones de persistencia para el dominio Message.
 */
public interface MessageDomainRepository {
    MessageDomain save(MessageDomain message);
    Optional<MessageDomain> findById(Integer id);
    List<MessageDomain> findAll();
    void deleteById(Integer id);
}
