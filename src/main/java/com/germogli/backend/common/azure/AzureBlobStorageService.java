package com.germogli.backend.common.azure;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.azure.storage.common.sas.SasProtocol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
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
     * @param containerName      Nombre del contenedor
     * @param blobName           Nombre del blob
     * @param expirationMinutes  Tiempo de expiración del token en minutos
     * @return URL firmada para acceso temporal
     */
    public String generateSasToken(String containerName, String blobName, int expirationMinutes) {
        try {
            // Obtener el cliente del contenedor
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);

            // Obtener el cliente del blob directamente sin codificar
            BlobClient blobClient = containerClient.getBlobClient(blobName);

            // Verificar si el blob existe
            boolean exists = blobClient.exists();
            System.out.println("Verificando blob '" + blobName + "': " + (exists ? "EXISTE" : "NO EXISTE"));

            if (!exists) {
                // Si no existe con el nombre tal cual, listar blobs para diagnóstico
                System.out.println("Blobs en el contenedor:");
                containerClient.listBlobs().iterator().forEachRemaining(
                        blob -> System.out.println(" - " + blob.getName())
                );

                return ""; // Retornar cadena vacía si no existe
            }

            // Definir permisos para el token SAS (lectura en este caso)
            BlobSasPermission sasPermission = new BlobSasPermission()
                    .setReadPermission(true);

            // Calcular tiempo de expiración
            OffsetDateTime expirationTime = OffsetDateTime.now().plusMinutes(expirationMinutes);

            // Generar valores para la firma SAS
            BlobServiceSasSignatureValues sasSignatureValues = new BlobServiceSasSignatureValues(expirationTime, sasPermission)
                    .setProtocol(SasProtocol.HTTPS_HTTP);

            // Generar el token SAS
            String sasToken = blobClient.generateSas(sasSignatureValues);

            // Construir URL con token SAS
            String fullUrl = blobClient.getBlobUrl() + "?" + sasToken;
            System.out.println("URL con SAS generada: " + fullUrl);

            return fullUrl;

        } catch (Exception e) {
            System.err.println("Error generando SAS token: " + e.getMessage());
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Sube un archivo a un contenedor en Azure Blob Storage.
     *
     * @param containerName Nombre del contenedor donde se almacenará el archivo.
     * @param blobName      Nombre del archivo (blob) dentro del contenedor.
     * @param data          Flujo de entrada (InputStream) con los datos del archivo.
     * @param length        Tamaño del archivo en bytes.
     */
    public String uploadFile(String containerName, String blobName, InputStream data, long length) {
        try {
            // Obtiene o crea el contenedor
            BlobContainerClient containerClient = getOrCreateContainer(containerName);

            // Obtiene una referencia al blob (archivo)
            BlobClient blobClient = containerClient.getBlobClient(blobName);

            // Sube el archivo al contenedor, sobrescribiendo si ya existe
            blobClient.upload(data, length, true);

            // Verificar que el blob se subió correctamente
            if (!blobClient.exists()) {
                throw new RuntimeException("El archivo no se pudo subir correctamente a Azure Blob Storage");
            }

            // Obtener y retornar la URL
            String blobUrl = blobClient.getBlobUrl();

            // Log para debugging
            System.out.println("Archivo subido exitosamente: " + blobName);
            System.out.println("URL generada: " + blobUrl);

            return blobUrl;

        } catch (Exception e) {
            System.err.println("Error al subir archivo a Azure: " + e.getMessage());
            throw new RuntimeException("Error al subir archivo a Azure Blob Storage", e);
        }
    }

    /**
     * Elimina un archivo (blob) de un contenedor en Azure Blob Storage.
     *
     * @param containerName Nombre del contenedor.
     * @param blobName      Nombre del archivo (blob) a eliminar.
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
    public BlobContainerClient getOrCreateContainer(String containerName) {
        // Obtiene el contenedor
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
        // Si el contenedor no existe, lo crea
        if (!containerClient.exists()) {
            containerClient.create();
        }
        return containerClient;
    }

    /**
     * Obtiene la URL pública de un blob dado el nombre del contenedor y el nombre del blob.
     *
     * @param containerName Nombre del contenedor.
     * @param blobName      Nombre del blob.
     * @return URL del blob.
     */
    public String getBlobUrl(String containerName, String blobName) {
        try {
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
            BlobClient blobClient = containerClient.getBlobClient(blobName);

            // Verificar que el blob existe
            if (!blobClient.exists()) {
                System.err.println("ADVERTENCIA: El blob no existe: " + blobName);
                return "";
            }

            String url = blobClient.getBlobUrl();
            System.out.println("URL obtenida para " + blobName + ": " + url);

            return url != null ? url : "";

        } catch (Exception e) {
            System.err.println("Error al obtener URL del blob: " + e.getMessage());
            return "";
        }
    }

    /**
     * Sube un archivo grande por bloques para optimizar el rendimiento.
     *
     * @param containerName Nombre del contenedor
     * @param blobName Nombre del archivo (blob)
     * @param inputStream Stream con los datos del archivo
     * @param length Tamaño del archivo en bytes
     * @return URL del blob subido
     */
    public String uploadLargeFile(String containerName, String blobName, InputStream inputStream, long length) throws IOException {
        BlobContainerClient containerClient = getOrCreateContainer(containerName);
        BlobClient blobClient = containerClient.getBlobClient(blobName);
        BlockBlobClient blockBlobClient = blobClient.getBlockBlobClient();

        // Tamaño de cada bloque (4MB)
        int blockSize = 4 * 1024 * 1024;
        byte[] buffer = new byte[blockSize];
        List<String> blockIds = new ArrayList<>();
        int blockIndex = 0;
        int bytesRead;

        System.out.println("Iniciando carga por bloques de archivo: " + blobName);

        while ((bytesRead = inputStream.read(buffer)) != -1) {
            String blockId = Base64.getEncoder().encodeToString(
                    String.format("%06d", blockIndex).getBytes(StandardCharsets.UTF_8));
            blockIds.add(blockId);

            if (bytesRead < buffer.length) {
                byte[] trimmedBuffer = Arrays.copyOf(buffer, bytesRead);
                blockBlobClient.stageBlock(blockId, new ByteArrayInputStream(trimmedBuffer), bytesRead);
            } else {
                blockBlobClient.stageBlock(blockId, new ByteArrayInputStream(buffer), bytesRead);
            }

            blockIndex++;
            System.out.println("Bloque " + blockIndex + " subido");
        }

        // Confirmar todos los bloques
        blockBlobClient.commitBlockList(blockIds);
        System.out.println("Carga por bloques completada: " + blobName);

        // Verificar que el blob existe después de la carga
        if (!blobClient.exists()) {
            throw new RuntimeException("El archivo no se pudo verificar después de la carga por bloques");
        }

        String url = blobClient.getBlobUrl();
        System.out.println("URL del archivo grande: " + url);

        return url;
    }
}
