package com.germogli.backend.education.guides.domain.service;

import com.germogli.backend.authentication.domain.model.UserDomain;
import com.germogli.backend.common.azure.service.AzureBlobStorageService;
import com.germogli.backend.common.exception.CustomForbiddenException;
import com.germogli.backend.education.domain.service.EducationSharedService;
import com.germogli.backend.education.guides.application.dto.CreateGuideRequestDTO;
import com.germogli.backend.education.guides.domain.model.GuideDomain;
import com.germogli.backend.education.guides.domain.repository.GuideDomainRepository;
import com.germogli.backend.education.module.domain.model.ModuleDomain;
import com.germogli.backend.education.module.domain.service.ModuleDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class GuideDomainService {

    // Dependencias inyectadas a través del constructor
    private final GuideDomainRepository guideDomainRepository; // Repositorio para interactuar con la base de datos
    private final ModuleDomainService moduleDomainService; // Servicio para gestionar los módulos educativos
    private final AzureBlobStorageService azureBlobStorageService; // Servicio para interactuar con Azure Blob Storage
    private final EducationSharedService educationSharedService; // Servicio compartido para funciones comunes relacionadas con la educación

    // Método para crear una nueva guía educativa
    public GuideDomain createGuide(CreateGuideRequestDTO dto) {
        try {
            // Obtener el usuario autenticado
            UserDomain currentUser = educationSharedService.getAuthenticatedUser();

            // Verificar si el usuario tiene el rol de "ADMINISTRADOR"
            if (!educationSharedService.hasRole(currentUser, "ADMINISTRADOR")) {
                throw new AccessDeniedException("El usuario no tiene permisos para crear guías.");
            }

            // Verificar que el módulo existe antes de proceder
            moduleDomainService.getModuleById(dto.getModuleId());

            // Subir el archivo PDF a Azure y obtener la URL correspondiente
            String pdfUrl = this.uploadPdfToAzure(dto.getPdfFile(), dto.getModuleId(), dto.getTitle());

            // Crear el objeto GuideDomain que representa la guía educativa
            GuideDomain guideDomain = GuideDomain.builder()
                    .moduleId(ModuleDomain.builder()
                            .moduleId(dto.getModuleId()) // Establecer el módulo asociado a la guía
                            .build())
                    .title(dto.getTitle())
                    .description(dto.getDescription())
                    .pdfFileName(dto.getPdfFile().getOriginalFilename())
                    .pdfUrl(pdfUrl)
                    .creationDate(LocalDateTime.now())
                    .build();

            // Guardar la guía en la base de datos utilizando un procedimiento almacenado
            return guideDomainRepository.createGuide(guideDomain);

        } catch (IOException e) {
            throw new RuntimeException("Error al subir el archivo PDF", e);
        }
    }

    // Método para subir el archivo PDF a Azure Blob Storage y devolver la URL del archivo
    private String uploadPdfToAzure(MultipartFile pdfFile, Integer moduleId, String title) throws IOException {
        // Verificar si el archivo PDF es nulo o está vacío
        if (pdfFile == null || pdfFile.isEmpty()) {
            throw new CustomForbiddenException("El archivo PDF no puede estar vacío");
        }

        // Validar el tipo de archivo (solo PDF permitido)
        if (!"application/pdf".equals(pdfFile.getContentType())) {
            throw new CustomForbiddenException("Solo se permiten archivos PDF");
        }

        // Generar un nombre de archivo único basado en el ID del módulo y el título
        String fileName = moduleId + "_" +
                title.replaceAll("\\s+", "_") // Reemplazar espacios por guiones bajos
                        .replaceAll("[^a-zA-Z0-9.-]", "") + ".pdf"; // Eliminar caracteres no permitidos

        // Subir el archivo a Azure Blob Storage en el contenedor "pdfs-educativos"
        azureBlobStorageService.uploadFile("pdfs-educativos", fileName, pdfFile.getInputStream(), pdfFile.getSize());

        // Retornar la URL pública del archivo subido a Azure
        return "https://germoglistorage.blob.core.windows.net/pdfs-educativos/" + fileName;
    }
}
