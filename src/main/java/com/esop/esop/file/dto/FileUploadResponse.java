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
