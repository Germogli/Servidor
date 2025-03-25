package com.germogli.backend.education.domain.model;

/**
 * Interfaz genérica para la conversión entre entidad y modelo de dominio.
 *
 * @param <D> Tipo del modelo de dominio.
 * @param <E> Tipo de la entidad de infraestructura.
 */
public interface Converter<D, E> {

    /**
     * Convierte la entidad en una instancia del modelo de dominio.
     *
     * @param entity Entidad a convertir.
     * @return Modelo de dominio resultante.
     */
    D fromEntity(E entity);

    /**
     * Convierte el modelo de dominio en una entidad para persistencia.
     *
     * @return Entidad resultante.
     */
    E toEntity();
}
