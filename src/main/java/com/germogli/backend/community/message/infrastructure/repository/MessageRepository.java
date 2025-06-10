package com.germogli.backend.community.message.infrastructure.repository;

import com.germogli.backend.community.message.domain.model.MessageDomain;
import com.germogli.backend.community.message.domain.repository.MessageDomainRepository;
import com.germogli.backend.community.message.infrastructure.entity.MessageEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.StoredProcedureQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementación del repositorio para el dominio Message utilizando procedimientos almacenados.
 */
@Repository("communityMessageRepository")
@RequiredArgsConstructor
public class MessageRepository implements MessageDomainRepository {

    @PersistenceContext
    private final EntityManager entityManager;

    /**
     * Guarda o actualiza un mensaje usando procedimientos almacenados.
     * Si el mensaje es nuevo (ID nulo), se crea con sp_create_message;
     * si ya existe, se actualiza con sp_update_message.
     */
    @Override
    @Transactional
    public MessageDomain save(MessageDomain message) {
        if (message.getId() == null) {
            // Crear mensaje mediante sp_create_message
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_create_message");

            // ✅ REGISTRAR TODOS LOS PARÁMETROS CORRECTAMENTE
            query.registerStoredProcedureParameter("p_post_id", Integer.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("p_user_id", Integer.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("p_content", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("p_thread_id", Integer.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("p_group_id", Integer.class, ParameterMode.IN);

            // ✅ IMPORTANTE: Registrar AMBOS parámetros de salida
            query.registerStoredProcedureParameter("p_message_id", Integer.class, ParameterMode.OUT);
            query.registerStoredProcedureParameter("p_creation_date", Timestamp.class, ParameterMode.OUT);

            // ✅ ESTABLECER PARÁMETROS DE ENTRADA con validación de nulos
            query.setParameter("p_post_id", message.getPostId());
            query.setParameter("p_user_id", message.getUserId());
            query.setParameter("p_content", message.getContent());
            query.setParameter("p_thread_id", message.getThreadId());
            query.setParameter("p_group_id", message.getGroupId());

            try {
                // ✅ EJECUTAR PROCEDIMIENTO
                query.execute();

                // ✅ OBTENER VALORES DE SALIDA
                Integer messageId = (Integer) query.getOutputParameterValue("p_message_id");
                Timestamp creationTimestamp = (Timestamp) query.getOutputParameterValue("p_creation_date");

                // ✅ VALIDAR QUE SE OBTUVIERON LOS VALORES
                if (messageId == null) {
                    throw new RuntimeException("El procedimiento almacenado no devolvió un ID válido");
                }

                // ✅ ESTABLECER VALORES EN EL OBJETO DOMINIO
                message.setId(messageId);
                message.setCreationDate(creationTimestamp != null
                        ? creationTimestamp.toLocalDateTime()
                        : LocalDateTime.now());

                return message;

            } catch (Exception e) {
                // ✅ LOGGING DETALLADO PARA DEBUGGING
                System.err.println("Error ejecutando sp_create_message:");
                System.err.println("  - postId: " + message.getPostId());
                System.err.println("  - userId: " + message.getUserId());
                System.err.println("  - content: " + message.getContent());
                System.err.println("  - threadId: " + message.getThreadId());
                System.err.println("  - groupId: " + message.getGroupId());
                System.err.println("  - Error: " + e.getMessage());

                throw new RuntimeException("Error al crear mensaje: " + e.getMessage(), e);
            }

        } else {
            // ✅ ACTUALIZAR mensaje mediante sp_update_message
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_update_message");
            query.registerStoredProcedureParameter("p_message_id", Integer.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("p_content", String.class, ParameterMode.IN);

            query.setParameter("p_message_id", message.getId());
            query.setParameter("p_content", message.getContent());

            try {
                query.execute();
                return message;
            } catch (Exception e) {
                throw new RuntimeException("Error al actualizar mensaje: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Busca un mensaje por su ID usando sp_get_message_by_id.
     */
    @Override
    public Optional<MessageDomain> findById(Integer id) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_get_message_by_id", MessageEntity.class);
        query.registerStoredProcedureParameter("p_message_id", Integer.class, ParameterMode.IN);
        query.setParameter("p_message_id", id);
        query.execute();
        List<MessageEntity> resultList = query.getResultList();
        if (resultList.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(MessageDomain.fromEntityStatic(resultList.get(0)));
    }

    /**
     * Obtiene todos los mensajes usando sp_get_all_messages.
     */
    @Override
    public List<MessageDomain> findAll() {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_get_all_messages", MessageEntity.class);
        query.execute();
        List<MessageEntity> resultList = query.getResultList();
        return resultList.stream().map(MessageDomain::fromEntityStatic).collect(Collectors.toList());
    }

    /**
     * Elimina un mensaje por su ID usando sp_delete_message.
     */
    @Override
    @Transactional
    public void deleteById(Integer id) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_delete_message");
        query.registerStoredProcedureParameter("p_message_id", Integer.class, ParameterMode.IN);
        query.setParameter("p_message_id", id);
        query.execute();
    }
}
