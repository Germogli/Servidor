package com.germogli.backend.common.azure.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.common.sas.SasProtocol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio para gestionar archivos en Azure Blob Storage.
 */
@Service
public class AzureBlobStorageService {

    // Inyección del cliente de servicio de Blob Storage, que maneja la conexión con Azure.
    @Autowired
    private BlobServiceClient blobServiceClient;

    /**
     * Genera un token SAS para un archivo específico.
     *
     * @param containerName Nombre del contenedor
     * @param blobName Nombre del blob
     * @param expirationMinutes Tiempo de expiración del token en minutos
     * @return URL firmada para acceso temporal
     */
    public String generateSasToken(String containerName, String blobName, int expirationMinutes) {
        // Obtener el cliente del contenedor
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);

        // Obtener el cliente del blob específico
        BlobClient blobClient = containerClient.getBlobClient(blobName);

        // Definir permisos para el token SAS (lectura en este caso)
        BlobSasPermission sasPermission = new BlobSasPermission()
                .setReadPermission(true);

        // Calcular tiempo de expiración
        OffsetDateTime expirationTime = OffsetDateTime.now().plusMinutes(expirationMinutes);

        // Generar valores para la firma SAS
        BlobServiceSasSignatureValues sasSignatureValues = new BlobServiceSasSignatureValues(expirationTime, sasPermission)
                .setProtocol(SasProtocol.HTTPS_ONLY);  // Solo permitir conexiones seguras

        // Generar el token SAS
        String sasToken = blobClient.generateSas(sasSignatureValues);

        // Construir URL con token SAS
        return blobClient.getBlobUrl() + "?" + sasToken;
    }

    /**
     * Sube un archivo a un contenedor en Azure Blob Storage.
     *
     * @param containerName Nombre del contenedor donde se almacenará el archivo.
     * @param blobName Nombre del archivo (blob) dentro del contenedor.
     * @param data Flujo de entrada (InputStream) con los datos del archivo.
     * @param length Tamaño del archivo en bytes.
     */
    public void uploadFile(String containerName, String blobName, InputStream data, long length) {
        // Obtiene o crea el contenedor
        BlobContainerClient containerClient = getOrCreateContainer(containerName);
        // Obtiene una referencia al blob (archivo)
        BlobClient blobClient = containerClient.getBlobClient(blobName);
        // Sube el archivo al contenedor, sobrescribiendo si ya existe
        blobClient.upload(data, length, true);
    }

    /**
     * Elimina un archivo (blob) de un contenedor en Azure Blob Storage.
     *
     * @param containerName Nombre del contenedor.
     * @param blobName Nombre del archivo (blob) a eliminar.
     */
    public void deleteBlob(String containerName, String blobName) {
        // Obtiene el contenedor
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
        // Obtiene una referencia al blob
        BlobClient blobClient = containerClient.getBlobClient(blobName);
        // Elimina el archivo (blob)
        blobClient.delete();
    }

    /**
     * Obtiene un contenedor de Azure Blob Storage. Si no existe, lo crea.
     *
     * @param containerName Nombre del contenedor.
     * @return Cliente del contenedor.
     */
    private BlobContainerClient getOrCreateContainer(String containerName) {
        // Obtiene el contenedor
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
        // Si el contenedor no existe, lo crea
        if (!containerClient.exists()) {
            containerClient.create();
        }
        return containerClient;
    }
}