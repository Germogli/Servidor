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

            // IMPORTANTE: NO codificar el nombre del blob aquí - Azure SDK lo hará por nosotros
            BlobClient blobClient = containerClient.getBlobClient(blobName);

            // Verificar si el blob existe y mostrar mensaje de diagnóstico
            boolean exists = blobClient.exists();
            System.out.println("Verificando blob '" + blobName + "': " + (exists ? "EXISTE" : "NO EXISTE"));

            if (!exists) {
                // Listar algunos blobs para diagnóstico
                System.out.println("Blobs en el contenedor:");
                containerClient.listBlobs().iterator().forEachRemaining(
                        blob -> System.out.println(" - " + blob.getName())
                );

                // Intentar con un nombre URL-encoded por si acaso
                String encodedName = URLEncoder.encode(blobName, StandardCharsets.UTF_8.name())
                        .replace("+", "%20"); // Importante: reemplazar + por %20 para espacios
                blobClient = containerClient.getBlobClient(encodedName);
                exists = blobClient.exists();
                System.out.println("Verificando blob codificado '" + encodedName + "': " + (exists ? "EXISTE" : "NO EXISTE"));
            }

            if (!exists) {
                return ""; // O retornar una URL por defecto
            }

            // Definir permisos para el token SAS (lectura en este caso)
            BlobSasPermission sasPermission = new BlobSasPermission()
                    .setReadPermission(true);

            // Calcular tiempo de expiración
            OffsetDateTime expirationTime = OffsetDateTime.now().plusMinutes(expirationMinutes);

            // Generar valores para la firma SAS
            BlobServiceSasSignatureValues sasSignatureValues = new BlobServiceSasSignatureValues(expirationTime, sasPermission)
                    .setProtocol(SasProtocol.HTTPS_HTTP);  // Permitir conexiones HTTP y HTTPS

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
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
        BlobClient blobClient = containerClient.getBlobClient(blobName);
        return blobClient.getBlobUrl();
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
        }

        blockBlobClient.commitBlockList(blockIds);
        return blobClient.getBlobUrl();
    }
}
