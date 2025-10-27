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
//        if (!metadata.getUploadedBy().equals(username)) {
//            log.warn("Unauthorized download attempt: user={}, file owner={}",
//                    username, metadata.getUploadedBy());
//            throw new SecurityException("You don't have permission to download this file");
//        }
        
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
