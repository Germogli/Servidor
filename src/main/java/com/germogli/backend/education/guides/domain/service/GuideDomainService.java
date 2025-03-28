package com.germogli.backend.education.guides.domain.service;

import com.germogli.backend.authentication.domain.model.UserDomain;
import com.germogli.backend.common.azure.service.AzureBlobStorageService;
import com.germogli.backend.common.exception.CustomForbiddenException;
import com.germogli.backend.common.exception.ResourceNotFoundException;
import com.germogli.backend.education.domain.service.EducationSharedService;
import com.germogli.backend.education.guides.application.dto.CreateGuideRequestDTO;
import com.germogli.backend.education.guides.application.dto.GuideResponseDTO;
import com.germogli.backend.education.guides.application.dto.UpdateGuideRequestDTO;
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
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class GuideDomainService {

    // Dependencias inyectadas a través del constructor
    private final GuideDomainRepository guideDomainRepository; // Repositorio para interactuar con la base de datos
    private final ModuleDomainService moduleDomainService; // Servicio para gestionar los módulos educativos
    private final AzureBlobStorageService azureBlobStorageService; // Servicio para interactuar con Azure Blob Storage
    private final EducationSharedService educationSharedService; // Servicio compartido para funciones comunes relacionadas con la educación

    /**
     * Actualiza los datos de una guía educativa en la base de datos.
     *
     * @param dto El objeto UpdateGuideRequest con la nueva información de la guía.
     * @return El objeto GuideDomain actualizado.
     */
    public GuideDomain updateGuide(Integer guideId, UpdateGuideRequestDTO dto) {
        // Obtener el usuario autenticado
        UserDomain currentUser = educationSharedService.getAuthenticatedUser();

        // Verificar si el usuario tiene el rol de "ADMINISTRADOR"
        if (!educationSharedService.hasRole(currentUser, "ADMINISTRADOR")) {
            throw new AccessDeniedException("El usuario no tiene permisos para crear guías.");
        }

        // Verificar que la guía existe; si no, lanzar una excepción
        GuideDomain existingGuide = guideDomainRepository.getById(guideId)
                .orElseThrow(() -> new ResourceNotFoundException("Guía no encontrada con id " + guideId));

        // Verificar si el módulo existe antes de actualizar
        if (dto.getModuleId() != null) {
            moduleDomainService.getModuleById(dto.getModuleId());
        }

        // Crear el objeto GuideDomain a partir del DTO de forma explícita
        GuideDomain guideDomain = GuideDomain.builder()
                .guideId(guideId)  // Establecer el ID de la guía
                .moduleId(ModuleDomain.builder()
                        .moduleId(dto.getModuleId())  // Establecer el módulo asociado a la guía
                        .build())
                .title(dto.getTitle())  // Establecer el título de la guía
                .description(dto.getDescription())  // Establecer la descripción de la guía
                .build();

        // Llamar al repositorio para realizar la actualización
        return guideDomainRepository.updateGuideInfo(guideDomain);
    }

    /**
     * Obtiene todas las publicaciones.
     *
     * @return Lista de guias.
     * @throws ResourceNotFoundException si no hay guias disponibles.
     */
    public List<GuideDomain> getAllGuides() {
        List<GuideDomain> guides = guideDomainRepository.getAll();
        if (guides.isEmpty()) {
            throw new ResourceNotFoundException("No hay guias disponibles.");
        }
        return guides;
    }

    /**
     * Obtiene las guías que pertenecen a un módulo específico, basado en su ID.
     *
     * @param moduleId ID del módulo para el cual se desean obtener las guías.
     * @return Lista de guías asociadas a dicho módulo.
     * @throws ResourceNotFoundException si no se encuentran guías para el módulo proporcionado.
     */
    public List<GuideDomain> getGuidesByModuleId(Integer moduleId) {
        List<GuideDomain> guides = guideDomainRepository.getByModuleId(moduleId);
        if (guides.isEmpty()) {
            throw new ResourceNotFoundException("No hay guias disponibles para este modulo.");
        }
        return guides;
    }

    /**
     * Obtiene una guía por su ID.
     *
     * @param id ID de la guía a buscar.
     * @return El objeto GuideDomain correspondiente si existe.
     * @throws ResourceNotFoundException si no se encuentra la guía con el ID proporcionado.
     */
    public GuideDomain getGuideById(Integer id) {
        return guideDomainRepository.getById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Guia no encontrada con id " + id));
    }

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

    // Método para obtener URL segura con SAS Token
    public String getSecureGuideUrl(GuideDomain guide) {
        // Extrae el nombre del archivo de la URL original
        String fileName = extractFileNameFromUrl(guide.getPdfUrl());

        // Genera token SAS con duración de 150 minutos
        return azureBlobStorageService.generateSasToken(
                "pdfs-educativos",
                fileName,
                150  // 150 minutos de duración
        );
    }

    // Método auxiliar para extraer nombre de archivo desde URL
    private String extractFileNameFromUrl(String url) {
        return url.substring(url.lastIndexOf("/") + 1);
    }

    /**
     * Convierte una lista de entidades de dominio a DTOs de respuesta.
     *
     * @param domains Lista de objetos GuideDomain que representan las guías en la capa de dominio.
     * @return Lista de objetos GuideResponseDTO con los datos formateados para la respuesta al cliente.
     */
    public List<GuideResponseDTO> toResponseList(List<GuideDomain> domains) {
        return domains.stream()
                .map(domain -> {
                    GuideResponseDTO dto = new GuideResponseDTO();
                    dto.setGuideId(domain.getGuideId());
                    dto.setTitle(domain.getTitle());
                    dto.setDescription(domain.getDescription());
                    // Generar URL segura con SAS Token
                    dto.setPdfUrl(getSecureGuideUrl(domain));
                    dto.setPdfFileName(domain.getPdfFileName());  // Copia explícita del nombre del archivo
                    dto.setCreationDate(domain.getCreationDate());
                    dto.setModuleId(domain.getModuleId() != null ? domain.getModuleId().getModuleId() : null);

                    // Devuelve el DTO convertido para el mapeo
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * Convierte una entidad de dominio a un DTO de respuesta.
     *
     * @param guide Guía de dominio
     * @return DTO de respuesta
     */
    public GuideResponseDTO toResponse(GuideDomain guide) {
        return GuideResponseDTO.builder()
                .guideId(guide.getGuideId())
                .moduleId(guide.getModuleId() != null ? guide.getModuleId().getModuleId() : null)
                .title(guide.getTitle())
                .description(guide.getDescription())
                .pdfUrl(guide.getPdfUrl())
                .creationDate(guide.getCreationDate())
                .build();
    }
}
