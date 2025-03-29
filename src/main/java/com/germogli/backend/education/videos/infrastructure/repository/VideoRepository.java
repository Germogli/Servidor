package com.germogli.backend.education.videos.infrastructure.repository;

import com.germogli.backend.education.videos.domain.model.VideoDomain;
import com.germogli.backend.education.videos.domain.repository.VideoDomainRepository;
import com.germogli.backend.education.videos.infrastructure.entity.VideoEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Repositorio para gestionar operaciones de videos utilizando procedimientos almacenados.
 * Implementa la interfaz VideoDomainRepository para realizar operaciones CRUD.
 */
@Repository("educationVideoRepository")
@RequiredArgsConstructor
public class VideoRepository implements VideoDomainRepository {

    // Instancia del EntityManager para interactuar con la base de datos
    private final EntityManager entityManager;

    /**
     * Crea un nuevo video en la base de datos utilizando un procedimiento almacenado.
     *
     * @param videoDomain El objeto VideoDomain que representa el video a crear.
     * @return El objeto VideoDomain creado con su ID generado.
     */
    @Override
    public VideoDomain createVideo(VideoDomain videoDomain) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_create_video", VideoEntity.class);
        query.registerStoredProcedureParameter("p_module_id", Integer.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_title", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_video_url", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_video_id", Integer.class, ParameterMode.OUT);

        query.setParameter("p_module_id", videoDomain.getModuleId().getModuleId());
        query.setParameter("p_title", videoDomain.getTitle());
        query.setParameter("p_video_url", videoDomain.getVideoUrl());

        query.execute();

        Integer generatedId = (Integer) query.getOutputParameterValue("p_video_id");
        videoDomain.setVideoId(generatedId);
        return videoDomain;
    }

    /**
     * Obtiene un video por su ID utilizando un procedimiento almacenado.
     *
     * @param videoId ID del video.
     * @return Optional de VideoDomain.
     */
    @Override
    public Optional<VideoDomain> getById(Integer videoId) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_get_video_by_id", VideoEntity.class);
        query.registerStoredProcedureParameter("p_video_id", Integer.class, ParameterMode.IN);
        query.setParameter("p_video_id", videoId);
        query.execute();
        List<VideoEntity> resultList = query.getResultList();
        if (resultList.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(VideoDomain.fromEntityStatic(resultList.get(0)));
        }
    }

    /**
     * Obtiene todos los videos que pertenecen a un módulo específico.
     *
     * @param moduleId ID del módulo.
     * @return Lista de VideoDomain.
     */
    @Override
    public List<VideoDomain> getVideosByModuleId(Integer moduleId) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_get_videos_by_module_id", VideoEntity.class);
        query.registerStoredProcedureParameter("p_module_id", Integer.class, ParameterMode.IN);
        query.setParameter("p_module_id", moduleId);
        query.execute();
        List<VideoEntity> resultList = query.getResultList();
        return resultList.stream().map(VideoDomain::fromEntityStatic).collect(Collectors.toList());
    }

    /**
     * Actualiza los datos de un video utilizando un procedimiento almacenado.
     *
     * @param videoDomain El objeto VideoDomain con la nueva información.
     * @return El objeto VideoDomain actualizado.
     */
    @Override
    public VideoDomain updateVideo(VideoDomain videoDomain) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_update_video_info");
        query.registerStoredProcedureParameter("p_video_id", Integer.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_module_id", Integer.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_title", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_video_url", String.class, ParameterMode.IN);

        query.setParameter("p_video_id", videoDomain.getVideoId());
        query.setParameter("p_module_id", videoDomain.getModuleId().getModuleId());
        query.setParameter("p_title", videoDomain.getTitle());
        query.setParameter("p_video_url", videoDomain.getVideoUrl());

        query.execute();
        return videoDomain;
    }

    /**
     * Elimina un video utilizando un procedimiento almacenado.
     *
     * @param videoId ID del video a eliminar.
     */
    @Override
    public void deleteVideo(Integer videoId) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_delete_video");
        query.registerStoredProcedureParameter("p_video_id", Integer.class, ParameterMode.IN);
        query.setParameter("p_video_id", videoId);
        query.execute();
    }
}
