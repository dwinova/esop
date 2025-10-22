#!/bin/bash

# Secure File Encryption Module Setup Script
# Run this script from your project root directory: bash setup-file-module.sh

set -e

echo "🚀 Setting up Secure File Encryption Module..."

# Define base path
BASE_PATH="src/main/java/com/esop/esop/file"
RESOURCES_PATH="src/main/resources"

# Create directory structure
echo "📁 Creating directory structure..."
mkdir -p "$BASE_PATH/config"
mkdir -p "$BASE_PATH/controller"
mkdir -p "$BASE_PATH/service"
mkdir -p "$BASE_PATH/repository"
mkdir -p "$BASE_PATH/entity"
mkdir -p "$BASE_PATH/dto"
mkdir -p "$BASE_PATH/exception"
mkdir -p "$RESOURCES_PATH/db/migration"

# ============================================
# CONFIG FILES
# ============================================

echo "⚙️  Creating configuration files..."

# VaultConfig.java
cat > "$BASE_PATH/config/VaultConfig.java" << 'EOF'
package com.esop.esop.file.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.vault.authentication.TokenAuthentication;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.core.VaultTemplate;

import java.net.URI;

/**
 * Vault configuration for Transit secrets engine.
 * Connects to HashiCorp Vault for encryption/decryption operations.
 */
@Configuration
public class VaultConfig {

    @Value("${vault.uri}")
    private String vaultUri;

    @Value("${vault.token}")
    private String vaultToken;

    @Value("${vault.transit.key-name}")
    private String transitKeyName;

    /**
     * Creates VaultTemplate bean for interacting with Vault API.
     */
    @Bean
    public VaultTemplate vaultTemplate() {
        VaultEndpoint endpoint = VaultEndpoint.from(URI.create(vaultUri));
        TokenAuthentication authentication = new TokenAuthentication(vaultToken);
        return new VaultTemplate(endpoint, authentication);
    }

    /**
     * Provides the Transit key name used for encryption/decryption.
     */
    @Bean
    public String transitKeyName() {
        return transitKeyName;
    }
}
EOF

# MinioConfig.java
cat > "$BASE_PATH/config/MinioConfig.java" << 'EOF'
package com.esop.esop.file.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO S3-compatible storage configuration.
 * Initializes MinIO client and ensures bucket exists.
 */
@Slf4j
@Configuration
public class MinioConfig {

    @Value("${minio.url}")
    private String minioUrl;

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    @Value("${minio.bucket}")
    private String bucketName;

    /**
     * Creates MinioClient bean and ensures bucket exists.
     */
    @Bean
    public MinioClient minioClient() {
        try {
            MinioClient client = MinioClient.builder()
                    .endpoint(minioUrl)
                    .credentials(accessKey, secretKey)
                    .build();

            // Check if bucket exists, create if not
            boolean bucketExists = client.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(bucketName)
                            .build()
            );

            if (!bucketExists) {
                client.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(bucketName)
                                .build()
                );
                log.info("Created MinIO bucket: {}", bucketName);
            }

            log.info("MinIO client initialized successfully");
            return client;
        } catch (Exception e) {
            log.error("Failed to initialize MinIO client", e);
            throw new RuntimeException("MinIO initialization failed", e);
        }
    }

    @Bean
    public String minioBucketName() {
        return bucketName;
    }
}
EOF

# ============================================
# ENTITY
# ============================================

echo "📦 Creating entity..."

cat > "$BASE_PATH/entity/FileMetadata.java" << 'EOF'
package com.esop.esop.file.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing metadata for encrypted files stored in MinIO.
 */
@Entity
@Table(name = "file_metadata")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String filename;

    @Column(name = "content_type")
    private String contentType;

    @Column(nullable = false)
    private Long size;

    @Column(nullable = false)
    private String checksum;

    @Column(name = "storage_path", nullable = false, unique = true)
    private String storagePath;

    @Column(name = "vault_key_version")
    private String vaultKeyVersion;

    @Column(name = "uploaded_by", nullable = false)
    private String uploadedBy;

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;

    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDateTime.now();
    }
}
EOF

# ============================================
# REPOSITORY
# ============================================

echo "🗄️  Creating repository..."

cat > "$BASE_PATH/repository/FileMetadataRepository.java" << 'EOF'
package com.esop.esop.file.repository;

import com.esop.esop.file.entity.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {
    
    List<FileMetadata> findByUploadedBy(String uploadedBy);
    
    List<FileMetadata> findByUploadedByOrderByUploadedAtDesc(String uploadedBy);
}
EOF

# ============================================
# EXCEPTIONS
# ============================================

echo "⚠️  Creating custom exceptions..."

cat > "$BASE_PATH/exception/EncryptionException.java" << 'EOF'
package com.esop.esop.file.exception;

public class EncryptionException extends RuntimeException {
    public EncryptionException(String message) {
        super(message);
    }

    public EncryptionException(String message, Throwable cause) {
        super(message, cause);
    }
}
EOF

cat > "$BASE_PATH/exception/StorageException.java" << 'EOF'
package com.esop.esop.file.exception;

public class StorageException extends RuntimeException {
    public StorageException(String message) {
        super(message);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
EOF

cat > "$BASE_PATH/exception/FileNotFoundException.java" << 'EOF'
package com.esop.esop.file.exception;

public class FileNotFoundException extends RuntimeException {
    public FileNotFoundException(String message) {
        super(message);
    }
}
EOF

# ============================================
# DTOs
# ============================================

echo "📋 Creating DTOs..."

cat > "$BASE_PATH/dto/FileUploadResponse.java" << 'EOF'
package com.esop.esop.file.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponse {
    private Long fileId;
    private String filename;
    private String contentType;
    private Long size;
    private String checksum;
    private LocalDateTime uploadedAt;
    private String message;
}
EOF

cat > "$BASE_PATH/dto/FileDownloadResponse.java" << 'EOF'
package com.esop.esop.file.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileDownloadResponse {
    private String filename;
    private String contentType;
    private byte[] data;
}
EOF

# ============================================
# SERVICES
# ============================================

echo "🔧 Creating service layer..."

cat > "$BASE_PATH/service/VaultEncryptionService.java" << 'EOF'
package com.esop.esop.file.service;

import com.esop.esop.file.exception.EncryptionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.Ciphertext;
import org.springframework.vault.support.Plaintext;

/**
 * Service for encrypting and decrypting data using Vault Transit secrets engine.
 * All encryption operations use AES-256 via Vault API.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VaultEncryptionService {

    private final VaultTemplate vaultTemplate;
    private final String transitKeyName;

    /**
     * Encrypts data using Vault Transit engine.
     * 
     * @param data Raw data to encrypt
     * @return Base64-encoded encrypted ciphertext with Vault prefix
     */
    public String encrypt(byte[] data) {
        try {
            // Create plaintext object from raw bytes
            Plaintext plaintext = Plaintext.of(data);
            
            // Encrypt using Vault Transit
            String ciphertext = vaultTemplate.opsForTransit()
                    .encrypt(transitKeyName, plaintext)
                    .getCiphertext();
            
            log.debug("Successfully encrypted {} bytes", data.length);
            return ciphertext;
            
        } catch (Exception e) {
            log.error("Encryption failed", e);
            throw new EncryptionException("Failed to encrypt data", e);
        }
    }

    /**
     * Decrypts data using Vault Transit engine.
     * 
     * @param ciphertext Encrypted data with Vault prefix (vault:v1:...)
     * @return Decrypted raw bytes
     */
    public byte[] decrypt(String ciphertext) {
        try {
            // Decrypt using Vault Transit
            Plaintext plaintext = vaultTemplate.opsForTransit()
                    .decrypt(transitKeyName, Ciphertext.of(ciphertext));
            
            byte[] decryptedData = plaintext.getPlaintext();
            log.debug("Successfully decrypted {} bytes", decryptedData.length);
            
            return decryptedData;
            
        } catch (Exception e) {
            log.error("Decryption failed", e);
            throw new EncryptionException("Failed to decrypt data", e);
        }
    }

    /**
     * Extracts Vault key version from ciphertext.
     * Format: vault:v1:base64data -> returns "v1"
     */
    public String extractKeyVersion(String ciphertext) {
        try {
            String[] parts = ciphertext.split(":");
            if (parts.length >= 2) {
                return parts[1];
            }
            return "unknown";
        } catch (Exception e) {
            log.warn("Could not extract key version from ciphertext", e);
            return "unknown";
        }
    }
}
EOF

cat > "$BASE_PATH/service/MinioStorageService.java" << 'EOF'
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
@RequiredArgsConstructor
public class MinioStorageService {

    private final MinioClient minioClient;
    private final String bucketName;

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
EOF

cat > "$BASE_PATH/service/FileMetadataService.java" << 'EOF'
package com.esop.esop.file.service;

import com.esop.esop.file.entity.FileMetadata;
import com.esop.esop.file.exception.FileNotFoundException;
import com.esop.esop.file.repository.FileMetadataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing file metadata in PostgreSQL.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileMetadataService {

    private final FileMetadataRepository repository;

    /**
     * Saves file metadata to database.
     */
    @Transactional
    public FileMetadata save(FileMetadata metadata) {
        FileMetadata saved = repository.save(metadata);
        log.info("Saved file metadata: id={}, filename={}", saved.getId(), saved.getFilename());
        return saved;
    }

    /**
     * Retrieves file metadata by ID.
     */
    public FileMetadata findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new FileNotFoundException("File not found with id: " + id));
    }

    /**
     * Lists all files uploaded by a specific user.
     */
    public List<FileMetadata> findByUser(String username) {
        return repository.findByUploadedByOrderByUploadedAtDesc(username);
    }

    /**
     * Deletes file metadata.
     */
    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
        log.info("Deleted file metadata: id={}", id);
    }
}
EOF

cat > "$BASE_PATH/service/SecureFileService.java" << 'EOF'
package com.esop.esop.file.service;

import com.esop.esop.file.entity.FileMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;

/**
 * Orchestration service for secure file operations.
 * Handles the complete flow: encrypt → upload → save metadata.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SecureFileService {

    private final VaultEncryptionService encryptionService;
    private final MinioStorageService storageService;
    private final FileMetadataService metadataService;

    /**
     * Encrypts and uploads a file securely.
     * 
     * Flow:
     * 1. Read file bytes
     * 2. Calculate SHA-256 checksum
     * 3. Encrypt with Vault AES-256
     * 4. Upload to MinIO
     * 5. Save metadata to PostgreSQL
     */
    @Transactional
    public FileMetadata encryptAndUpload(MultipartFile file, String username) {
        try {
            log.info("Starting secure upload for file: {}, user: {}", 
                    file.getOriginalFilename(), username);

            // Step 1: Read file bytes
            byte[] originalData = file.getBytes();
            
            // Step 2: Calculate checksum of original file
            String checksum = calculateChecksum(originalData);
            
            // Step 3: Encrypt file with Vault
            String encryptedData = encryptionService.encrypt(originalData);
            
            // Step 4: Upload encrypted data to MinIO
            String storagePath = storageService.upload(
                    encryptedData.getBytes(),
                    file.getOriginalFilename(),
                    file.getContentType()
            );
            
            // Step 5: Extract Vault key version
            String keyVersion = encryptionService.extractKeyVersion(encryptedData);
            
            // Step 6: Save metadata to PostgreSQL
            FileMetadata metadata = FileMetadata.builder()
                    .filename(file.getOriginalFilename())
                    .contentType(file.getContentType())
                    .size(file.getSize())
                    .checksum(checksum)
                    .storagePath(storagePath)
                    .vaultKeyVersion(keyVersion)
                    .uploadedBy(username)
                    .build();
            
            FileMetadata saved = metadataService.save(metadata);
            
            log.info("Successfully uploaded encrypted file: id={}, storage={}", 
                    saved.getId(), storagePath);
            
            return saved;
            
        } catch (IOException e) {
            log.error("Failed to read file", e);
            throw new RuntimeException("Failed to process file", e);
        }
    }

    /**
     * Downloads and decrypts a file.
     */
    @Transactional(readOnly = true)
    public byte[] downloadAndDecrypt(Long fileId, String username) {
        log.info("Starting secure download for fileId: {}, user: {}", fileId, username);

        // Step 1: Fetch metadata
        FileMetadata metadata = metadataService.findById(fileId);
        
        // Security check: ensure user owns the file
        if (!metadata.getUploadedBy().equals(username)) {
            log.warn("Unauthorized download attempt: user={}, file owner={}", 
                    username, metadata.getUploadedBy());
            throw new SecurityException("You don't have permission to download this file");
        }
        
        // Step 2: Download encrypted file from MinIO
        byte[] encryptedData = storageService.download(metadata.getStoragePath());
        
        // Step 3: Decrypt with Vault
        byte[] decryptedData = encryptionService.decrypt(new String(encryptedData));
        
        log.info("Successfully downloaded and decrypted file: id={}", fileId);
        
        return decryptedData;
    }

    /**
     * Get file metadata.
     */
    public FileMetadata getMetadata(Long fileId) {
        return metadataService.findById(fileId);
    }

    /**
     * List user's files.
     */
    public List<FileMetadata> listUserFiles(String username) {
        return metadataService.findByUser(username);
    }

    /**
     * Deletes a file and its metadata.
     */
    @Transactional
    public void deleteFile(Long fileId, String username) {
        FileMetadata metadata = metadataService.findById(fileId);
        
        // Security check
        if (!metadata.getUploadedBy().equals(username)) {
            throw new SecurityException("You don't have permission to delete this file");
        }
        
        // Delete from MinIO
        storageService.delete(metadata.getStoragePath());
        
        // Delete metadata
        metadataService.delete(fileId);
        
        log.info("Deleted file: id={}", fileId);
    }

    /**
     * Calculates SHA-256 checksum of file data.
     */
    private String calculateChecksum(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
EOF

# ============================================
# CONTROLLER
# ============================================

echo "🎮 Creating controller..."

cat > "$BASE_PATH/controller/SecureFileController.java" << 'EOF'
package com.esop.esop.file.controller;

import com.esop.esop.file.dto.FileUploadResponse;
import com.esop.esop.file.entity.FileMetadata;
import com.esop.esop.file.service.SecureFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * REST API for secure file upload/download operations.
 * All endpoints require JWT authentication.
 */
@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Tag(name = "Secure File Management", description = "APIs for encrypted file storage")
@SecurityRequirement(name = "bearerAuth")
public class SecureFileController {

    private final SecureFileService secureFileService;

    /**
     * Encrypts and uploads a sensitive file.
     * POST /api/files/encrypt-upload
     */
    @PostMapping(value = "/encrypt-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Upload encrypted file",
            description = "Encrypts file with AES-256 via Vault and stores in MinIO"
    )
    public ResponseEntity<FileUploadResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        
        log.info("Upload request received: filename={}, size={} bytes", 
                file.getOriginalFilename(), file.getSize());

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        String username = authentication.getName();
        FileMetadata metadata = secureFileService.encryptAndUpload(file, username);
        
        FileUploadResponse response = FileUploadResponse.builder()
                .fileId(metadata.getId())
                .filename(metadata.getFilename())
                .contentType(metadata.getContentType())
                .size(metadata.getSize())
                .checksum(metadata.getChecksum())
                .uploadedAt(metadata.getUploadedAt())
                .message("File uploaded and encrypted successfully")
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Downloads and decrypts a file.
     * GET /api/files/{id}/download
     */
    @GetMapping("/{id}/download")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Download decrypted file",
            description = "Retrieves encrypted file from MinIO and decrypts with Vault"
    )
    public ResponseEntity<Resource> downloadFile(
            @PathVariable Long id,
            Authentication authentication) {
        
        log.info("Download request received: fileId={}", id);

        String username = authentication.getName();
        FileMetadata metadata = secureFileService.getMetadata(id);
        byte[] fileData = secureFileService.downloadAndDecrypt(id, username);
        
        ByteArrayResource resource = new ByteArrayResource(fileData);
        
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        metadata.getContentType() != null 
                        ? metadata.getContentType() 
                        : "application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"" + metadata.getFilename() + "\"")
                .contentLength(fileData.length)
                .body(resource);
    }

    /**
     * List user's files.
     * GET /api/files
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List user files", description = "Get all files uploaded by current user")
    public ResponseEntity<List<FileMetadata>> listFiles(Authentication authentication) {
        String username = authentication.getName();
        List<FileMetadata> files = secureFileService.listUserFiles(username);
        return ResponseEntity.ok(files);
    }

    /**
     * Deletes a file.
     * DELETE /api/files/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete file", description = "Removes file from MinIO and database")
    public ResponseEntity<Void> deleteFile(
            @PathVariable Long id,
            Authentication authentication) {
        
        log.info("Delete request received: fileId={}", id);
        
        String username = authentication.getName();
        secureFileService.deleteFile(id, username);
        
        return ResponseEntity.noContent().build();
    }
}
EOF

# ============================================
# DATABASE MIGRATION
# ============================================

echo "🗃️  Creating database migration..."

cat > "$RESOURCES_PATH/db/migration/V2__create_file_metadata_table.sql" << 'EOF'
-- Create file_metadata table for encrypted file storage
CREATE TABLE IF NOT EXISTS file_metadata (
    id BIGSERIAL PRIMARY KEY,
    filename VARCHAR(255) NOT NULL,
    content_type VARCHAR(128),
    size BIGINT NOT NULL,
    checksum VARCHAR(128) NOT NULL,
    storage_path VARCHAR(512) NOT NULL,
    vault_key_version VARCHAR(64),
    uploaded_by VARCHAR(128) NOT NULL,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_storage_path UNIQUE (storage_path)
);

-- Create indexes for faster queries
CREATE INDEX idx_uploaded_by ON file_metadata(uploaded_by);
CREATE INDEX idx_uploaded_at ON file_metadata(uploaded_at);

-- Add comments
COMMENT ON TABLE file_metadata IS 'Stores metadata for encrypted files in MinIO';
COMMENT ON COLUMN file_metadata.checksum IS 'SHA-256 checksum of original file';
COMMENT ON COLUMN file_metadata.vault_key_version IS 'Vault Transit key version used for encryption';
EOF

# ============================================
# APPLICATION.YML ADDITIONS
# ============================================

echo "📝 Creating application.yml template..."

cat > "$RESOURCES_PATH/application-file-module.yml" << 'EOF'
# Add these configurations to your main application.yml

spring:
  servlet:
    multipart:
      max-file-size: 50MB
      max-request-size: 50MB

# Vault Configuration
vault:
  uri: ${VAULT_URI:http://localhost:8200}
  token: ${VAULT_TOKEN:root}
  transit:
    enabled: true
    key-name: ${VAULT_KEY_NAME:aes256-key}

# MinIO Configuration
minio:
  url: ${MINIO_URL:http
