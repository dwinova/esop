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
