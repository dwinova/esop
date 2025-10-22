package com.esop.esop.file.service;

import com.esop.esop.file.exception.StorageException;
import io.minio.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.UUID;

/**
 * Service for uploading and downloading files to/from MinIO storage.
 * All files are stored encrypted.
 */
@Slf4j
@Service
public class MinioStorageService {

    private final MinioClient minioClient;
    private final String bucketName;

    public MinioStorageService(
            MinioClient minioClient,
            @org.springframework.beans.factory.annotation.Qualifier("minioBucketName") String bucketName) {
        this.minioClient = minioClient;
        this.bucketName = bucketName;
    }

    /**
     * Uploads encrypted file data to MinIO.
     * 
     * @param encryptedData Encrypted file bytes
     * @param originalFilename Original filename
     * @param contentType MIME type
     * @return Storage path (object name in MinIO)
     */
    public String upload(byte[] encryptedData, String originalFilename, String contentType) {
        try {
            // Generate unique storage path
            String objectName = generateObjectName(originalFilename);
            
            // Upload to MinIO
            try (InputStream stream = new ByteArrayInputStream(encryptedData)) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(objectName)
                                .stream(stream, encryptedData.length, -1)
                                .contentType(contentType != null ? contentType : "application/octet-stream")
                                .build()
                );
            }
            
            log.info("Uploaded file to MinIO: {}", objectName);
            return objectName;
            
        } catch (Exception e) {
            log.error("Failed to upload file to MinIO", e);
            throw new StorageException("Failed to upload file", e);
        }
    }

    /**
     * Downloads encrypted file from MinIO.
     * 
     * @param storagePath Object name in MinIO
     * @return Encrypted file bytes
     */
    public byte[] download(String storagePath) {
        try {
            // Download from MinIO
            try (InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(storagePath)
                            .build()
            )) {
                byte[] data = stream.readAllBytes();
                log.info("Downloaded file from MinIO: {}", storagePath);
                return data;
            }
            
        } catch (Exception e) {
            log.error("Failed to download file from MinIO: {}", storagePath, e);
            throw new StorageException("Failed to download file", e);
        }
    }

    /**
     * Deletes file from MinIO.
     * 
     * @param storagePath Object name in MinIO
     */
    public void delete(String storagePath) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(storagePath)
                            .build()
            );
            log.info("Deleted file from MinIO: {}", storagePath);
            
        } catch (Exception e) {
            log.error("Failed to delete file from MinIO: {}", storagePath, e);
            throw new StorageException("Failed to delete file", e);
        }
    }

    /**
     * Generates unique object name for MinIO storage.
     * Format: encrypted/{year}/{month}/{uuid}_{filename}
     */
    private String generateObjectName(String originalFilename) {
        String uuid = UUID.randomUUID().toString();
        String sanitizedFilename = originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
        
        java.time.LocalDate now = java.time.LocalDate.now();
        return String.format("encrypted/%d/%02d/%s_%s",
                now.getYear(),
                now.getMonthValue(),
                uuid,
                sanitizedFilename);
    }
}
