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
