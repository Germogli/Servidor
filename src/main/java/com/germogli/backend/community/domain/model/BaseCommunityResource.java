package com.germogli.backend.community.domain.model;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Clase base para todos los modelos de dominio de Community.
 * Proporciona atributos comunes (por ejemplo, creationDate) que heredarán los modelos concretos.
 */
@Getter
@Setter
@SuperBuilder
public abstract class BaseCommunityResource {
    /**
     * Fecha de creación del recurso.
     * Se establece por defecto a la fecha y hora actual.
     */
    @lombok.Builder.Default
    private LocalDateTime creationDate = LocalDateTime.now();
}
