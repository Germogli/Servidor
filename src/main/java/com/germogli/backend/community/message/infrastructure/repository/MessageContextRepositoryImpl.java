
package com.germogli.backend.community.message.infrastructure.repository;

import com.germogli.backend.community.message.domain.model.MessageDomain;
import com.germogli.backend.community.message.domain.repository.MessageContextRepository;
import com.germogli.backend.community.message.infrastructure.entity.MessageEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.StoredProcedureQuery;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación del repositorio para consultas contextuales de mensajes
 * utilizando procedimientos almacenados.
 */
@Repository
public class MessageContextRepositoryImpl implements MessageContextRepository {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Busca mensajes basados en el tipo de contexto y su identificador.
     * Utiliza un procedimiento almacenado garantizado de solo lectura para ser compatible
     * con transacciones marcadas como readOnly=true.
     *
     * @param contextType Tipo de contexto (group, thread, post, forum)
     * @param contextId   ID del contexto específico
     * @param limit       Número máximo de mensajes a recuperar
     * @param offset      Punto de inicio para paginación
     * @return Lista de mensajes del contexto solicitado
     */
    @Override
    public List<MessageDomain> findMessagesByContext(String contextType, Integer contextId, int limit, int offset) {
        try {
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery(
                    "sp_get_messages_by_context_readonly", MessageEntity.class);

            query.registerStoredProcedureParameter("p_context_type", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("p_context_id", Integer.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("p_limit", Integer.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("p_offset", Integer.class, ParameterMode.IN);

            query.setParameter("p_context_type", contextType);
            query.setParameter("p_context_id", contextId);
            query.setParameter("p_limit", limit);
            query.setParameter("p_offset", offset);

            query.execute();

            @SuppressWarnings("unchecked")
            List<MessageEntity> entities = query.getResultList();

            // Convertir entidades a objetos de dominio
            List<MessageDomain> messages = new ArrayList<>();
            for (MessageEntity entity : entities) {
                messages.add(MessageDomain.fromEntityStatic(entity));
            }

            return messages;
        } catch (Exception e) {
            // Registrar el error con información detallada
            throw new RuntimeException("Error al recuperar mensajes por contexto: " + e.getMessage(), e);
        }
    }
}