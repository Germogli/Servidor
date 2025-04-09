package com.germogli.backend.common.config;

import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de Azure Blob Storage para la aplicación.
 * Esta clase se encarga de crear y proporcionar un cliente de Azure Blob Storage
 * a través de un Bean en el contexto de Spring.
 */
@Configuration
public class AzureBlobStorageConfig {

    // Obtiene la cadena de conexión desde el archivo de configuración (application.properties)
    @Value("${azure.storage.connection-string}")
    private String connectionString;

    /**
     * Crea y expone un Bean de BlobServiceClient.
     * Este cliente permite interactuar con el servicio de almacenamiento de blobs en Azure.
     *
     * @return una instancia de BlobServiceClient configurada con la cadena de conexión.
     */
    @Bean
    public BlobServiceClient blobServiceClient() {
        return new BlobServiceClientBuilder()
                .connectionString(connectionString) // Usa la cadena de conexión configurada
                .buildClient(); // Construye y retorna el cliente
    }
}
