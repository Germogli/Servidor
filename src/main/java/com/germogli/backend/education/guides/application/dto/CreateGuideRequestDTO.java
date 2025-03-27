package com.germogli.backend.education.guides.application.dto;

import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * DTO para la creación de una guía.
 * Este objeto se utiliza para recibir los datos de la solicitud HTTP
 * cuando se crea una nueva guía, incluyendo el archivo PDF que se sube.
 */
@Data
@Builder
public class CreateGuideRequestDTO {
    private Integer moduleId;
    private String title;
    private String description;

    /**
     * Archivo PDF que se sube en la solicitud.
     *
     * La interfaz MultipartFile representa un archivo cargado en una solicitud HTTP.
     * Permite acceder al contenido del archivo, su nombre, tipo, tamaño, etc.
     * Esto es útil para manejar la carga de archivos de forma sencilla en Spring.
     */
    private MultipartFile pdfFile;
}
