package com.germogli.backend.monitoring.crop.infrastructure.repository;

import com.germogli.backend.monitoring.crop.domain.model.CropDomain;
import com.germogli.backend.monitoring.crop.domain.repository.CropDomainRepository;
import com.germogli.backend.monitoring.crop.infrastructure.entity.CropEntity;
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
 * Implementación de CropDomainRepository utilizando procedimientos almacenados.
 */
@Repository
@RequiredArgsConstructor
public class CropRepository implements CropDomainRepository {

    @PersistenceContext
    private final EntityManager entityManager;

    /**
     * Guarda o actualiza un cultivo.
     * Utiliza los procedimientos almacenados sp_create_crop o sp_update_crop
     * según el cultivo tenga ID o no.
     */
    @Override
    @Transactional
    public CropDomain save(CropDomain crop) {
        if (crop.getId() == null) {
            // Crear cultivo usando sp_create_crop
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_create_crop");
            query.registerStoredProcedureParameter("p_user_id", Integer.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("p_crop_name", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("p_crop_type", String.class, ParameterMode.IN);

            query.setParameter("p_user_id", crop.getUserId());
            query.setParameter("p_crop_name", crop.getCropName());
            query.setParameter("p_crop_type", crop.getCropType());

            query.execute();
            return crop;
        } else {
            // Actualizar cultivo usando sp_update_crop
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_update_crop");
            query.registerStoredProcedureParameter("p_crop_id", Integer.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("p_crop_name", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("p_crop_type", String.class, ParameterMode.IN);

            query.setParameter("p_crop_id", crop.getId());
            query.setParameter("p_crop_name", crop.getCropName());
            query.setParameter("p_crop_type", crop.getCropType());

            query.execute();
            return crop;
        }
    }

    /**
     * Busca un cultivo por su ID.
     * Utiliza consulta JPA directa ya que no hay un procedimiento almacenado equivalente.
     */
    @Override
    public Optional<CropDomain> findById(Integer id) {
        CropEntity entity = entityManager.find(CropEntity.class, id);
        return Optional.ofNullable(entity).map(CropDomain::fromEntityStatic);
    }

    /**
     * Obtiene todos los cultivos.
     * Utiliza consulta JPA directa ya que no hay un procedimiento almacenado equivalente.
     */
    @Override
    public List<CropDomain> findAll() {
        List<CropEntity> entities = entityManager.createQuery("SELECT c FROM CropEntity c", CropEntity.class)
                .getResultList();
        return entities.stream().map(CropDomain::fromEntityStatic).collect(Collectors.toList());
    }

    /**
     * Elimina un cultivo por su ID.
     * Utiliza consulta JPA directa ya que no hay un procedimiento almacenado equivalente.
     */
    @Override
    @Transactional
    public void deleteById(Integer id) {
        entityManager.createQuery("DELETE FROM CropEntity c WHERE c.id = :id")
                .setParameter("id", id)
                .executeUpdate();
    }

    /**
     * Encuentra todos los cultivos de un usuario específico.
     * Utiliza consulta JPA directa ya que no hay un procedimiento almacenado equivalente.
     */
    @Override
    public List<CropDomain> findByUserId(Integer userId) {
        List<CropEntity> entities = entityManager.createQuery(
                        "SELECT c FROM CropEntity c WHERE c.userId = :userId", CropEntity.class)
                .setParameter("userId", userId)
                .getResultList();
        return entities.stream().map(CropDomain::fromEntityStatic).collect(Collectors.toList());
    }
}