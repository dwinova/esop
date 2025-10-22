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
