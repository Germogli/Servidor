package com.germogli.backend.monitoring.crop.domain.repository;

import com.germogli.backend.monitoring.crop.domain.model.CropDomain;

import java.util.List;
import java.util.Optional;

/**
 * Interfaz para las operaciones de persistencia del dominio Crop.
 * Define las operaciones básicas de persistencia para cultivos.
 */
public interface CropDomainRepository {
    /**
     * Guarda un cultivo en la base de datos.
     *
     * @param crop Cultivo a guardar.
     * @return Cultivo guardado con su ID asignado.
     */
    CropDomain save(CropDomain crop);

    /**
     * Busca un cultivo por su ID.
     *
     * @param id Identificador del cultivo.
     * @return Cultivo encontrado o empty si no existe.
     */
    Optional<CropDomain> findById(Integer id);

    /**
     * Obtiene todos los cultivos.
     *
     * @return Lista de todos los cultivos.
     */
    List<CropDomain> findAll();

    /**
     * Elimina un cultivo por su ID.
     *
     * @param id Identificador del cultivo a eliminar.
     */
    void deleteById(Integer id);

    /**
     * Encuentra todos los cultivos de un usuario específico.
     *
     * @param userId Identificador del usuario.
     * @return Lista de cultivos del usuario.
     */
    List<CropDomain> findByUserId(Integer userId);
}